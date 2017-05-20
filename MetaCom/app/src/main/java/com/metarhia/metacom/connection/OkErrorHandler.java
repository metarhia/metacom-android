package com.metarhia.metacom.connection;

import com.metarhia.jstp.compiler.annotations.Indexed;
import com.metarhia.jstp.compiler.annotations.JSTPHandler;
import com.metarhia.jstp.compiler.annotations.Named;
import com.metarhia.jstp.compiler.annotations.NotNull;
import com.metarhia.jstp.core.JSTypes.JSArray;

/**
 * JSTP handler
 *
 * @author lidaamber
 */

@JSTPHandler
public interface OkErrorHandler {

    @NotNull
    @Named("ok")
    void onOk(JSArray args);

    @NotNull
    @Named("error")
    void onError(@Indexed(0) Integer errorCode);
}
