package think.webglmap.bukkit.web;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import think.webglmap.bukkit.WebglMapPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.logging.Logger;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@RequiredArgsConstructor
public class HTTPHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final static Logger logger = Logger.getLogger(HTTPHandler.class.getName());
    private final static HashMap<String, String> mimeTypes = new HashMap<String, String>();
    static {
        mimeTypes.put("html", "text/html");
        mimeTypes.put("js", "application/javascript");
        mimeTypes.put("css", "text/css");
    }

    private final WebglMapPlugin plugin;
    private final int id;

    @Override
    protected void channelRead0(ChannelHandlerContext context, FullHttpRequest msg) throws Exception {
        httpRequest(context, (FullHttpRequest) msg);
    }

    public void httpRequest(ChannelHandlerContext context, FullHttpRequest request) throws IOException {
        if (!request.getDecoderResult().isSuccess()) {
            sendHttpResponse(context, request, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        if (request.getMethod() != GET) {
            sendHttpResponse(context, request, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }

        if (request.getUri().equals("/")) {
            request.setUri("/index.html");
        }

        InputStream stream = this.getClass().getClassLoader().getResourceAsStream("www" + request.getUri());
        if (stream == null) {
            logger.info("404 - www" + request.getUri());
            sendHttpResponse(context, request, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND));
            return;
        }
        ByteBufOutputStream out = new ByteBufOutputStream(Unpooled.buffer());
        IOUtils.copy(stream, out);
        stream.close();
        out.close();
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, out.buffer());

        String ext = request.getUri().substring(request.getUri().lastIndexOf('.') + 1);
        String type = mimeTypes.containsKey(ext) ? mimeTypes.get(ext) : "text/plain";
        if (type.startsWith("text/")) {
            type += "; charset=UTF-8";
        }
        response.headers().set(CONTENT_TYPE, type);
        setContentLength(response, response.content().readableBytes());
        sendHttpResponse(context, request, response);

    }

    public void sendHttpResponse(ChannelHandlerContext context, FullHttpRequest request, FullHttpResponse response) {
        if (response.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(response.getStatus().toString(), CharsetUtil.UTF_8);
            response.content().writeBytes(buf);
            buf.release();
            setContentLength(response, response.content().readableBytes());
        }

        ChannelFuture future = context.channel().writeAndFlush(response);
        if (!isKeepAlive(request) || response.getStatus().code() != 200) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }
}