package org.apache.catalina.connector.http;


import java.io.EOFException;
import java.io.InterruptedIOException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeMap;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Globals;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Logger;
import org.apache.catalina.util.FastHttpDateFormat;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.util.ServerInfo;
import org.apache.catalina.util.StringManager;
import org.apache.catalina.util.StringParser;


/**
 * Implementation of a request processor (and its associated thread) that may
 * be used by an HttpConnector to process individual requests.  The connector
 * will allocate a processor from its pool, assign a particular socket to it,
 * and the processor will then execute the processing required to complete
 * the request.  When the processor is completed, it will recycle itself.
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 * @version $Revision: 1.46 $ $Date: 2002/04/04 17:50:34 $
 * @deprecated
 */

final class HttpProcessor
    implements Lifecycle, Runnable {


    // ----------------------------------------------------- Manifest Constants


    /**
     * Server information string for this server.
     */
    private static final String SERVER_INFO =
        ServerInfo.getServerInfo() + " (HTTP/1.1 Connector)";


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new HttpProcessor associated with the specified connector.
     *
     * @param connector HttpConnector that owns this processor
     * @param id Identifier of this HttpProcessor (unique per connector)
     */
    public HttpProcessor(HttpConnector connector, int id) {

        super();
        this.connector = connector;
        this.debug = connector.getDebug();
        this.id = id;
        this.proxyName = connector.getProxyName();
        this.proxyPort = connector.getProxyPort();
        this.request = (HttpRequestImpl) connector.createRequest();
        this.response = (HttpResponseImpl) connector.createResponse();
        this.serverPort = connector.getPort();
        this.threadName =
          "HttpProcessor[" + connector.getPort() + "][" + id + "]";

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Is there a new socket available?
     */
    private boolean available = false;


    /**
     * The HttpConnector with which this processor is associated.
     */
    private HttpConnector connector = null;


    /**
     * The debugging detail level for this component.
     */
    private int debug = 0;


    /**
     * The identifier of this processor, unique per connector.
     */
    private int id = 0;


    /**
     * The lifecycle event support for this component.
     */
    private LifecycleSupport lifecycle = new LifecycleSupport(this);


    /**
     * The match string for identifying a session ID parameter.
     */
    private static final String match =
        ";" + Globals.SESSION_PARAMETER_NAME + "=";


    /**
     * The match string for identifying a session ID parameter.
     */
    private static final char[] SESSION_ID = match.toCharArray();


    /**
     * The string parser we will use for parsing request lines.
     */
    private StringParser parser = new StringParser();


    /**
     * The proxy server name for our Connector.
     */
    private String proxyName = null;


    /**
     * The proxy server port for our Connector.
     */
    private int proxyPort = 0;


    /**
     * The HTTP request object we will pass to our associated container.
     */
    private HttpRequestImpl request = null;


    /**
     * The HTTP response object we will pass to our associated container.
     */
    private HttpResponseImpl response = null;


    /**
     * The actual server port for our Connector.
     */
    private int serverPort = 0;


    /**
     * The string manager for this package.
     */
    protected StringManager sm =
        StringManager.getManager(Constants.Package);


    /**
     * The socket we are currently processing a request for.  This object
     * is used for inter-thread communication only.
     */
    private Socket socket = null;


    /**
     * Has this component been started yet?
     */
    private boolean started = false;


    /**
     * The shutdown signal to our background thread
     */
    private boolean stopped = false;


    /**
     * The background thread.
     */
    private Thread thread = null;


    /**
     * The name to register for the background thread.
     */
    private String threadName = null;


    /**
     * The thread synchronization object.
     */
    private Object threadSync = new Object();


    /**
     * Keep alive indicator.
     */
    private boolean keepAlive = false;


    /**
     * HTTP/1.1 client.
     */
    private boolean http11 = true;


    /**
     * True if the client has asked to recieve a request acknoledgement. If so
     * the server will send a preliminary 100 Continue response just after it
     * has successfully parsed the request headers, and before starting
     * reading the request entity body.
     */
    private boolean sendAck = false;


    /**
     * Ack string when pipelining HTTP requests.
     */
    private static final byte[] ack =
        (new String("HTTP/1.1 100 Continue\r\n\r\n")).getBytes();


    /**
     * CRLF.
     */
    private static final byte[] CRLF = (new String("\r\n")).getBytes();


    /**
     * Line buffer.
     */
    //private char[] lineBuffer = new char[4096];


    /**
     * Request line buffer.
     */
    private HttpRequestLine requestLine = new HttpRequestLine();


    /**
     * Processor state
     */
    private int status = Constants.PROCESSOR_IDLE;


    // --------------------------------------------------------- Public Methods


    /**
     * Return a String value representing this object.
     */
    public String toString() {

        return (this.threadName);

    }


    // -------------------------------------------------------- Package Methods


    /**
     * Process an incoming TCP/IP connection on the specified socket.  Any
     * exception that occurs during processing must be logged and swallowed.
     * <b>NOTE</b>:  This method is called from our Connector's thread.  We
     * must assign it to our own thread so that multiple simultaneous
     * requests can be handled.
     *
     * @param socket TCP socket to process
     */
    synchronized void assign(Socket socket) {

        // Wait for the Processor to get the previous Socket
    	//available是指现在是不是可以获取到套接字（当前的处理器）,如果可以获取到就说明当前套接字前面有在处理的其他套接字，所以他需要等待
    	//Is there a new socket available?
        while (available) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        //前边的套接字处理完了就把当前的处理器的关联套接字设为当前分配过来的套接字，同时标记当前处理器已经有了处理的套接字并唤醒自己de线程
        // Store the newly available Socket and notify our thread
        this.socket = socket;
        available = true;//
        //Wakes up all threads that are waiting on this object's monitor. A thread waits on an object's monitor by calling one of the wait methods. 
        notifyAll();

        if ((debug >= 1) && (socket != null))
            log(" An incoming request is being assigned");

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Await a newly assigned Socket from our Connector, or <code>null</code>
     * if we are supposed to shut down.
     */
    private synchronized Socket await() {

        // Wait for the Connector to provide a new Socket
    	//处理器当前没有套接字时候，那么当期线程就等待直到有新套接字对象来的时候
    	//赋值当前处理器的套接字
    	//设置当前是否有可用套接字为false以便于连接器分配套接字
    	//唤醒自己线程
        while (!available) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }

        // Notify the Connector that we have received this Socket
        //使用本地套接字实例
        Socket socket = this.socket;
        available = false;
        notifyAll();

        if ((debug >= 1) && (socket != null))
            log("  The incoming request has been awaited");

        return (socket);

    }



    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     */
    private void log(String message) {

        Logger logger = connector.getContainer().getLogger();
        if (logger != null)
            logger.log(threadName + " " + message);

    }


    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    private void log(String message, Throwable throwable) {

        Logger logger = connector.getContainer().getLogger();
        if (logger != null)
            logger.log(threadName + " " + message, throwable);

    }


    /**
     * Parse the value of an <code>Accept-Language</code> header, and add
     * the corresponding Locales to the current request.
     *
     * @param value The value of the <code>Accept-Language</code> header.
     */
    private void parseAcceptLanguage(String value) {

        // Store the accumulated languages that have been requested in
        // a local collection, sorted by the quality value (so we can
        // add Locales in descending order).  The values will be ArrayLists
        // containing the corresponding Locales to be added
        TreeMap locales = new TreeMap();

        // Preprocess the value to remove all whitespace
        int white = value.indexOf(' ');
        if (white < 0)
            white = value.indexOf('\t');
        if (white >= 0) {
            StringBuffer sb = new StringBuffer();
            int len = value.length();
            for (int i = 0; i < len; i++) {
                char ch = value.charAt(i);
                if ((ch != ' ') && (ch != '\t'))
                    sb.append(ch);
            }
            value = sb.toString();
        }

        // Process each comma-delimited language specification
        parser.setString(value);        // ASSERT: parser is available to us
        int length = parser.getLength();
        while (true) {

            // Extract the next comma-delimited entry
            int start = parser.getIndex();
            if (start >= length)
                break;
            int end = parser.findChar(',');
            String entry = parser.extract(start, end).trim();
            parser.advance();   // For the following entry

            // Extract the quality factor for this entry
            double quality = 1.0;
            int semi = entry.indexOf(";q=");
            if (semi >= 0) {
                try {
                    quality = Double.parseDouble(entry.substring(semi + 3));
                } catch (NumberFormatException e) {
                    quality = 0.0;
                }
                entry = entry.substring(0, semi);
            }

            // Skip entries we are not going to keep track of
            if (quality < 0.00005)
                continue;       // Zero (or effectively zero) quality factors
            if ("*".equals(entry))
                continue;       // FIXME - "*" entries are not handled

            // Extract the language and country for this entry
            String language = null;
            String country = null;
            String variant = null;
            int dash = entry.indexOf('-');
            if (dash < 0) {
                language = entry;
                country = "";
                variant = "";
            } else {
                language = entry.substring(0, dash);
                country = entry.substring(dash + 1);
                int vDash = country.indexOf('-');
                if (vDash > 0) {
                    String cTemp = country.substring(0, vDash);
                    variant = country.substring(vDash + 1);
                    country = cTemp;
                } else {
                    variant = "";
                }
            }

            // Add a new Locale to the list of Locales for this quality level
            Locale locale = new Locale(language, country, variant);
            Double key = new Double(-quality);  // Reverse the order
            ArrayList values = (ArrayList) locales.get(key);
            if (values == null) {
                values = new ArrayList();
                locales.put(key, values);
            }
            values.add(locale);

        }

        // Process the quality values in highest->lowest order (due to
        // negating the Double value when creating the key)
        Iterator keys = locales.keySet().iterator();
        while (keys.hasNext()) {
            Double key = (Double) keys.next();
            ArrayList list = (ArrayList) locales.get(key);
            Iterator values = list.iterator();
            while (values.hasNext()) {
                Locale locale = (Locale) values.next();
                if (debug >= 1)
                    log(" Adding locale '" + locale + "'");
                request.addLocale(locale);
            }
        }

    }


    /**
     * Parse and record the connection parameters related to this request.
     *
     * @param socket The socket on which we are connected
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a parsing error occurs
     */
    private void parseConnection(Socket socket)
        throws IOException, ServletException {

        if (debug >= 2)
            log("  parseConnection: address=" + socket.getInetAddress() +
                ", port=" + connector.getPort());
        ((HttpRequestImpl) request).setInet(socket.getInetAddress());
        if (proxyPort != 0)
            request.setServerPort(proxyPort);
        else
            request.setServerPort(serverPort);
        request.setSocket(socket);

    }


    /**
     * Parse the incoming HTTP request headers, and set the appropriate
     * request headers.
     *
     * @param input The input stream connected to our socket
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a parsing error occurs
     */
    private void parseHeaders(SocketInputStream input)
        throws IOException, ServletException {

        while (true) {

        	//分配头部实例
            HttpHeader header = request.allocateHeader();

            // Read the next header
            input.readHeader(header);
            if (header.nameEnd == 0) {
                if (header.valueEnd == 0) {
                    return;
                } else {
                    throw new ServletException
                        (sm.getString("httpProcessor.parseHeaders.colon"));
                }
            }

            String value = new String(header.value, 0, header.valueEnd);
            if (debug >= 1)
                log(" Header " + new String(header.name, 0, header.nameEnd)
                    + " = " + value);

            // Set the corresponding request headers
            //为什么用header和字符数组进行比较
            if (header.equals(DefaultHeaders.AUTHORIZATION_NAME)) {
                request.setAuthorization(value);
            } else if (header.equals(DefaultHeaders.ACCEPT_LANGUAGE_NAME)) {
                parseAcceptLanguage(value);
            } else if (header.equals(DefaultHeaders.COOKIE_NAME)) {
                Cookie cookies[] = RequestUtil.parseCookieHeader(value);
                for (int i = 0; i < cookies.length; i++) {
                    if (cookies[i].getName().equals
                        (Globals.SESSION_COOKIE_NAME)) {
                        // Override anything requested in the URL
                        if (!request.isRequestedSessionIdFromCookie()) {
                            // Accept only the first session id cookie
                            request.setRequestedSessionId
                                (cookies[i].getValue());
                            request.setRequestedSessionCookie(true);
                            request.setRequestedSessionURL(false);
                            if (debug >= 1)
                                log(" Requested cookie session id is " +
                                    ((HttpServletRequest) request.getRequest())
                                    .getRequestedSessionId());
                        }
                    }
                    if (debug >= 1)
                        log(" Adding cookie " + cookies[i].getName() + "=" +
                            cookies[i].getValue());
                    request.addCookie(cookies[i]);
                }
            } else if (header.equals(DefaultHeaders.CONTENT_LENGTH_NAME)) {
                int n = -1;
                try {
                    n = Integer.parseInt(value);
                } catch (Exception e) {
                    throw new ServletException
                        (sm.getString
                         ("httpProcessor.parseHeaders.contentLength"));
                }
                request.setContentLength(n);
            } else if (header.equals(DefaultHeaders.CONTENT_TYPE_NAME)) {
                request.setContentType(value);
            } else if (header.equals(DefaultHeaders.HOST_NAME)) {
                int n = value.indexOf(':');
                if (n < 0) {
                    if (connector.getScheme().equals("http")) {
                        request.setServerPort(80);
                    } else if (connector.getScheme().equals("https")) {
                        request.setServerPort(443);
                    }
                    if (proxyName != null)
                        request.setServerName(proxyName);
                    else
                        request.setServerName(value);
                } else {
                    if (proxyName != null)
                        request.setServerName(proxyName);
                    else
                        request.setServerName(value.substring(0, n).trim());
                    if (proxyPort != 0)
                        request.setServerPort(proxyPort);
                    else {
                        int port = 80;
                        try {
                            port =
                                Integer.parseInt(value.substring(n+1).trim());
                        } catch (Exception e) {
                            throw new ServletException
                                (sm.getString
                                 ("httpProcessor.parseHeaders.portNumber"));
                        }
                        request.setServerPort(port);
                    }
                }
            } else if (header.equals(DefaultHeaders.CONNECTION_NAME)) {
                if (header.valueEquals
                    (DefaultHeaders.CONNECTION_CLOSE_VALUE)) {
                    keepAlive = false;
                    response.setHeader("Connection", "close");
                }
                //request.setConnection(header);
                /*
                  if ("keep-alive".equalsIgnoreCase(value)) {
                  keepAlive = true;
                  }
                */
            } else if (header.equals(DefaultHeaders.EXPECT_NAME)) {
                if (header.valueEquals(DefaultHeaders.EXPECT_100_VALUE))
                    sendAck = true;
                else
                    throw new ServletException
                        (sm.getString
                         ("httpProcessor.parseHeaders.unknownExpectation"));
            } else if (header.equals(DefaultHeaders.TRANSFER_ENCODING_NAME)) {
                //request.setTransferEncoding(header);
            }

            request.nextHeader();

        }

    }


    /**
     * Parse the incoming HTTP request and set the corresponding HTTP request
     * properties.
     *
     * @param input The input stream attached to our socket
     * @param output The output stream of the socket
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a parsing error occurs
     */
    private void parseRequest(SocketInputStream input, OutputStream output)
        throws IOException, ServletException {

        // Parse the incoming request line
        input.readRequestLine(requestLine);

        // When the previous method returns, we're actually processing a
        // request
        status = Constants.PROCESSOR_ACTIVE;

        String method =
            new String(requestLine.method, 0, requestLine.methodEnd);
        String uri = null;
        String protocol = new String(requestLine.protocol, 0,
                                     requestLine.protocolEnd);

        //System.out.println(" Method:" + method + "_ Uri:" + uri
        //                   + "_ Protocol:" + protocol);

        if (protocol.length() == 0)
            protocol = "HTTP/0.9";

        // Now check if the connection should be kept alive after parsing the
        // request.
        if ( protocol.equals("HTTP/1.1") ) {
            http11 = true;
            sendAck = false;
        } else {
            http11 = false;
            sendAck = false;
            // For HTTP/1.0, connection are not persistent by default,
            // unless specified with a Connection: Keep-Alive header.
            keepAlive = false;
        }

        // Validate the incoming request line
        if (method.length() < 1) {
            throw new ServletException
                (sm.getString("httpProcessor.parseRequest.method"));
        } else if (requestLine.uriEnd < 1) {
            throw new ServletException
                (sm.getString("httpProcessor.parseRequest.uri"));
        }

        // Parse any query parameters out of the request URI
        int question = requestLine.indexOf("?");
        if (question >= 0) {
            request.setQueryString
                (new String(requestLine.uri, question + 1,
                            requestLine.uriEnd - question - 1));
            if (debug >= 1)
                log(" Query string is " +
                    ((HttpServletRequest) request.getRequest())
                    .getQueryString());
            uri = new String(requestLine.uri, 0, question);
        } else {
            request.setQueryString(null);
            uri = new String(requestLine.uri, 0, requestLine.uriEnd);
        }

        // Checking for an absolute URI (with the HTTP protocol)
        if (!uri.startsWith("/")) {
            int pos = uri.indexOf("://");
            // Parsing out protocol and host name
            if (pos != -1) {
                pos = uri.indexOf('/', pos + 3);
                if (pos == -1) {
                    uri = "";
                } else {
                    uri = uri.substring(pos);
                }
            }
        }

        // Parse any requested session ID out of the request URI
        int semicolon = uri.indexOf(match);
        if (semicolon >= 0) {
            String rest = uri.substring(semicolon + match.length());
            int semicolon2 = rest.indexOf(';');
            if (semicolon2 >= 0) {
                request.setRequestedSessionId(rest.substring(0, semicolon2));
                rest = rest.substring(semicolon2);
            } else {
                request.setRequestedSessionId(rest);
                rest = "";
            }
            request.setRequestedSessionURL(true);
            uri = uri.substring(0, semicolon) + rest;
            if (debug >= 1)
                log(" Requested URL session id is " +
                    ((HttpServletRequest) request.getRequest())
                    .getRequestedSessionId());
        } else {
            request.setRequestedSessionId(null);
            request.setRequestedSessionURL(false);
        }

        // Normalize URI (using String operations at the moment)
        String normalizedUri = normalize(uri);
        if (debug >= 1)
            log("Normalized: '" + uri + "' to '" + normalizedUri + "'");

        // Set the corresponding request properties
        ((HttpRequest) request).setMethod(method);
        request.setProtocol(protocol);
        if (normalizedUri != null) {
            ((HttpRequest) request).setRequestURI(normalizedUri);
        } else {
            ((HttpRequest) request).setRequestURI(uri);
        }
        request.setSecure(connector.getSecure());
        request.setScheme(connector.getScheme());

        if (normalizedUri == null) {
            log(" Invalid request URI: '" + uri + "'");
            throw new ServletException("Invalid URI: " + uri + "'");
        }

        if (debug >= 1)
            log(" Request is '" + method + "' for '" + uri +
                "' with protocol '" + protocol + "'");

    }


    /**
     * Return a context-relative path, beginning with a "/", that represents
     * the canonical version of the specified path after ".." and "." elements
     * are resolved out.  If the specified path attempts to go outside the
     * boundaries of the current context (i.e. too many ".." path elements
     * are present), return <code>null</code> instead.
     *
     * @param path Path to be normalized
     */
    protected String normalize(String path) {

        if (path == null)
            return null;

        // Create a place for the normalized path
        String normalized = path;

        // Normalize "/%7E" and "/%7e" at the beginning to "/~"
        if (normalized.startsWith("/%7E") ||
            normalized.startsWith("/%7e"))
            normalized = "/~" + normalized.substring(4);

        // Prevent encoding '%', '/', '.' and '\', which are special reserved
        // characters
        if ((normalized.indexOf("%25") >= 0)
            || (normalized.indexOf("%2F") >= 0)
            || (normalized.indexOf("%2E") >= 0)
            || (normalized.indexOf("%5C") >= 0)
            || (normalized.indexOf("%2f") >= 0)
            || (normalized.indexOf("%2e") >= 0)
            || (normalized.indexOf("%5c") >= 0)) {
            return null;
        }

        if (normalized.equals("/."))
            return "/";

        // Normalize the slashes and add leading slash if necessary
        if (normalized.indexOf('\\') >= 0)
            normalized = normalized.replace('\\', '/');
        if (!normalized.startsWith("/"))
            normalized = "/" + normalized;

        // Resolve occurrences of "//" in the normalized path
        while (true) {
            int index = normalized.indexOf("//");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index) +
                normalized.substring(index + 1);
        }

        // Resolve occurrences of "/./" in the normalized path
        while (true) {
            int index = normalized.indexOf("/./");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index) +
                normalized.substring(index + 2);
        }

        // Resolve occurrences of "/../" in the normalized path
        while (true) {
            int index = normalized.indexOf("/../");
            if (index < 0)
                break;
            if (index == 0)
                return (null);  // Trying to go outside our context
            int index2 = normalized.lastIndexOf('/', index - 1);
            normalized = normalized.substring(0, index2) +
                normalized.substring(index + 3);
        }

        // Declare occurrences of "/..." (three or more dots) to be invalid
        // (on some Windows platforms this walks the directory tree!!!)
        if (normalized.indexOf("/...") >= 0)
            return (null);

        // Return the normalized path that we have completed
        return (normalized);

    }


    /**
     * Send a confirmation that a request has been processed when pipelining.
     * HTTP/1.1 100 Continue is sent back to the client.
     *
     * @param output Socket output stream
     */
    private void ackRequest(OutputStream output)
        throws IOException {
        if (sendAck)
            output.write(ack);
    }


    /**
     * Process an incoming HTTP request on the Socket that has been assigned
     * to this Processor.  Any exceptions that occur during processing must be
     * swallowed and dealt with.
     *
     * @param socket The socket on which we are connected to the client
     */
    private void process(Socket socket) {
        boolean ok = true;// ok 来指代在处理过程中是否发现错误,赋值异常
        boolean finishResponse = true;// 来指代 Response 接口中的 finishResponse 方法是否应该被调用
        SocketInputStream input = null;
        OutputStream output = null;

        // Construct and initialize the objects we will need
        try {
            input = new SocketInputStream(socket.getInputStream(),
                                          connector.getBufferSize());//获取客户端传入的缓冲字节大小
        } catch (Exception e) {
            log("process.create", e);
            ok = false;
        }

        keepAlive = true;//keepAlive 表示连接是否是持久的
        //stopped 表示 HttpProcessor 实例是否已经被连接器终止来确认 process 是否也应该停止，

        //保持对的流的写操作
        while (!stopped && ok && keepAlive) {

            finishResponse = true;

            try {
            	//request 在HttpProcess实例的时候创建
                request.setStream(input);
                request.setResponse(response);
                output = socket.getOutputStream();
                response.setStream(output);
                response.setRequest(request);
                ((HttpServletResponse) response.getResponse()).setHeader
                    ("Server", SERVER_INFO);
            } catch (Exception e) {
                log("process.create", e);
                ok = false;
            }


            // Parse the incoming request
            try {
                if (ok) {

                    parseConnection(socket);
                    parseRequest(input, output);
                    if (!request.getRequest().getProtocol()
                        .startsWith("HTTP/0"))
                        parseHeaders(input);
                    //http11 表示 从 web 客户端过来的 HTTP 请求是否支持 HTTP 1.1
                    if (http11) {
                        // Sending a request acknowledge back to the client if
                        // requested.
                    	//Send a confirmation that a request has been processed when pipelining.
                    	//它将会测试组块是否是允许的
                        ackRequest(output);
                        // If the protocol is HTTP/1.1, chunking is allowed.
                        //
                        if (connector.isChunkingAllowed())
                            response.setAllowChunking(true);
                    }

                }
            } catch (EOFException e) {
                // It's very likely to be a socket disconnect on either the
                // client or the server
                ok = false;
                finishResponse = false;
            } catch (ServletException e) {
                ok = false;
                try {
                    ((HttpServletResponse) response.getResponse())
                        .sendError(HttpServletResponse.SC_BAD_REQUEST);
                } catch (Exception f) {
                    ;
                }
            } catch (InterruptedIOException e) {
                if (debug > 1) {
                    try {
                        log("process.parse", e);
                        ((HttpServletResponse) response.getResponse())
                            .sendError(HttpServletResponse.SC_BAD_REQUEST);
                    } catch (Exception f) {
                        ;
                    }
                }
                ok = false;
            } catch (Exception e) {
                try {
                    log("process.parse", e);
                    ((HttpServletResponse) response.getResponse()).sendError
                        (HttpServletResponse.SC_BAD_REQUEST);
                } catch (Exception f) {
                    ;
                }
                ok = false;
            }

            // Ask our Container to process this request
            try {
                ((HttpServletResponse) response).setHeader
                    ("Date", FastHttpDateFormat.getCurrentDate());
                if (ok) {
                	//===============================传递给容器处理,容器先给基础阀门===================
                	//============================================================
                	//============================================================
                	//============================================================
                    connector.getContainer().invoke(request, response);
                    //传递给 SimpleWrapper（初始化时候传入）--》SimplePipeline--》内部类SimplePipelineValveContext的invokenext（属于管道和阀门直接的连接处，依次调用所有阀门，基础阀门在管道实例的时候已经设置）
                    //最后传给基础阀门---》SimpleWrapperValve （实例servelet）调用方法
                    //============================================================
                    //============================================================
                    //============================================================
                    //============================================================
                }
            } catch (ServletException e) {
                log("process.invoke", e);
                try {
                    ((HttpServletResponse) response.getResponse()).sendError
                        (HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                } catch (Exception f) {
                    ;
                }
                ok = false;
            } catch (InterruptedIOException e) {
                ok = false;
            } catch (Throwable e) {
                log("process.invoke", e);
                try {
                    ((HttpServletResponse) response.getResponse()).sendError
                        (HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                } catch (Exception f) {
                    ;
                }
                ok = false;
            }

            // Finish up the handling of the request
            System.out.println("finishResponse:"+finishResponse);
            if (finishResponse) {
                try {
                    response.finishResponse();
                } catch (IOException e) {
                    ok = false;
                } catch (Throwable e) {
                    log("process.invoke", e);
                    ok = false;
                }
                try {
                    request.finishRequest();
                } catch (IOException e) {
                    ok = false;
                } catch (Throwable e) {
                    log("process.invoke", e);
                    ok = false;
                }
                try {
                    if (output != null)
                        output.flush();
                } catch (IOException e) {
                    ok = false;
                }
            }

            // We have to check if the connection closure has been requested
            // by the application or the response stream (in case of HTTP/1.0
            // and keep-alive).
            if ( "close".equals(response.getHeader("Connection")) ) {
                keepAlive = false;
            }

            // End of request processing
            status = Constants.PROCESSOR_IDLE;

            // Recycling the request and the response objects
            request.recycle();
            response.recycle();

        }

        try {
            shutdownInput(input);
            socket.close();
        } catch (IOException e) {
            ;
        } catch (Throwable e) {
            log("process.invoke", e);
        }
        socket = null;

        System.out.println("结束");

    }


    protected void shutdownInput(InputStream input) {
        try {
            int available = input.available();
            // skip any unread (bogus) bytes
            if (available > 0) {
                input.skip(available);
            }
        } catch (Throwable e) {
            ;
        }
    }


    // ---------------------------------------------- Background Thread Methods


    /**
     * The background thread that listens for incoming TCP/IP connections and
     * hands them off to an appropriate processor.
     */
    public void run() {

        // Process requests until we receive a shutdown signal
        while (!stopped) {

        	//处理器run的时候等待新的套接字来处理
            // Wait for the next socket to be assigned
            Socket socket = await();
            if (socket == null)
                continue;

            // Process the request from this socket
            try {
            	//run方法里边处理套接字
                process(socket);
            } catch (Throwable t) {
                log("process.invoke", t);
            }

           //两个线程HttpConnector,HttpProcessor通过available联系
            //连接器回收处理器
            // Finish up this request
            connector.recycle(this);

        }

        // Tell threadStop() we have shut ourselves down successfully
        synchronized (threadSync) {
            threadSync.notifyAll();
        }

    }


    /**
     * Start the background processing thread.
     */
    private void threadStart() {

        log(sm.getString("httpProcessor.starting"));

        thread = new Thread(this, threadName);
        thread.setDaemon(true);
        thread.start();

        if (debug >= 1)
            log(" Background thread has been started");

    }


    /**
     * Stop the background processing thread.
     */
    private void threadStop() {

        log(sm.getString("httpProcessor.stopping"));

        stopped = true;
        assign(null);

        if (status != Constants.PROCESSOR_IDLE) {
            // Only wait if the processor is actually processing a command
            synchronized (threadSync) {
                try {
                    threadSync.wait(5000);
                } catch (InterruptedException e) {
                    ;
                }
            }
        }
        thread = null;

    }


    // ------------------------------------------------------ Lifecycle Methods


    /**
     * Add a lifecycle event listener to this component.
     *
     * @param listener The listener to add
     */
    public void addLifecycleListener(LifecycleListener listener) {

        lifecycle.addLifecycleListener(listener);

    }


    /**
     * Get the lifecycle listeners associated with this lifecycle. If this
     * Lifecycle has no listeners registered, a zero-length array is returned.
     */
    public LifecycleListener[] findLifecycleListeners() {

        return lifecycle.findLifecycleListeners();

    }


    /**
     * Remove a lifecycle event listener from this component.
     *
     * @param listener The listener to add
     */
    public void removeLifecycleListener(LifecycleListener listener) {

        lifecycle.removeLifecycleListener(listener);

    }


    /**
     * Start the background thread we will use for request processing.
     *
     * @exception LifecycleException if a fatal startup error occurs
     */
    public void start() throws LifecycleException {

        if (started)
            throw new LifecycleException
                (sm.getString("httpProcessor.alreadyStarted"));
        lifecycle.fireLifecycleEvent(START_EVENT, null);
        started = true;

        threadStart();

    }


    /**
     * Stop the background thread we will use for request processing.
     *
     * @exception LifecycleException if a fatal shutdown error occurs
     */
    public void stop() throws LifecycleException {

        if (!started)
            throw new LifecycleException
                (sm.getString("httpProcessor.notStarted"));
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;

        threadStop();

    }


}
