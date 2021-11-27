package com.geekbrains;

import com.geekgrains.common.Message;

import java.io.IOException;

public interface Callback {

    void call(Message msg) throws IOException;

}
