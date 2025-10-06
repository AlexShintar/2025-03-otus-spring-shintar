package ru.otus.hw.error;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class GlobalExceptionHandler extends AbstractErrorWebExceptionHandler {

    public GlobalExceptionHandler(
            GlobalErrorAttributes errorAttributes,
            ApplicationContext applicationContext,
            ServerCodecConfigurer codecConfigurer,
            ObjectProvider<ViewResolver> viewResolvers
    ) {
        super(errorAttributes, new WebProperties.Resources(), applicationContext);
        setMessageWriters(codecConfigurer.getWriters());
        setMessageReaders(codecConfigurer.getReaders());
        setViewResolvers(viewResolvers.orderedStream().toList());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorView);
    }

    private Mono<ServerResponse> renderErrorView(ServerRequest request) {
        Map<String, Object> model = getErrorAttributes(request, ErrorAttributeOptions.defaults());
        int status = (int) model.getOrDefault("status", 500);
        return ServerResponse.status(status)
                .contentType(MediaType.TEXT_HTML)
                .render("error", model);
    }
}
