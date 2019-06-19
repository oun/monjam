package com.monjam.core.api;

public interface Migration {
    void up(Context context);
    void down(Context context);
}
