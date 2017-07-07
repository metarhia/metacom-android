package com.metarhia.metacom.connection;

import com.metarhia.jstp.compiler.annotations.handlers.Array;
import com.metarhia.jstp.compiler.annotations.handlers.Handler;
import com.metarhia.jstp.compiler.annotations.handlers.NotNull;
import com.metarhia.jstp.compiler.annotations.handlers.Object;
import com.metarhia.jstp.handlers.ExecutableHandler;

import java.util.List;

/**
 * JSTP handler
 *
 * @author lidaamber
 */

@Handler(ExecutableHandler.class)
public interface OkErrorHandler {

    @NotNull
    @Object("ok")
    void onOk(List<?> args);

    @NotNull
    @Object("error")
    void onError(@Array(0) Integer errorCode);
}
