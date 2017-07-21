import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static io.netty.buffer.Unpooled.copiedBuffer;

/**
 * Created by SeanWu on 2017/7/21.
 */
public class HttpChannelHandler<C extends Channel> extends ChannelInitializer<C> {

    @Override
    protected void initChannel(C ch) throws Exception {
        ch.pipeline().addLast("codec", new HttpServerCodec());
        ch.pipeline().addLast("aggregator", new HttpObjectAggregator(512 * 1024));
        ch.pipeline().addLast("request", new ChannelInboundHandlerAdapter() {
            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                super.channelActive(ctx);
                System.out.println("Connected!");
            }

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                if (msg instanceof FullHttpRequest) {
                    FullHttpRequest request = (FullHttpRequest) msg;
                    String responseMessage = "Hello World";
                    HttpHeaders headers = request.headers();
                    ByteBuf content = request.content();
                    System.out.println(content.toString(StandardCharsets.UTF_8));
                    String uri = request.uri();
                    System.out.println("http uri: " + uri);
                    //去除浏览器"/favicon.ico"的干扰
                    if(uri.equals("/favicon.ico")){
                        return;
                    }
                    HttpMethod method = request.method();
                    if(method.equals(HttpMethod.GET)){
                        QueryStringDecoder queryString = new QueryStringDecoder(uri);
                        Map<String, List<String>> parameters = queryString.parameters();
                        System.out.println(JSON.toJSONString(parameters));

                    }else if(method.equals(HttpMethod.POST)){

                    }else{

                    }




                    DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                            HttpVersion.HTTP_1_1,
                            HttpResponseStatus.OK,
                            copiedBuffer(responseMessage.getBytes())
                    );

                    if (HttpUtil.isKeepAlive(request)) {
                        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                        response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN);
                        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, responseMessage.length());
                        ctx.writeAndFlush(response);
                    }
                } else {
                    // TODO ?????
                    super.channelRead(ctx, msg);
                }
            }

            @Override
            public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
                ctx.flush();
            }


            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                ctx.writeAndFlush(new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.INTERNAL_SERVER_ERROR,
                        copiedBuffer(cause.getMessage().getBytes())
                ));
            }
        });

    }

}
