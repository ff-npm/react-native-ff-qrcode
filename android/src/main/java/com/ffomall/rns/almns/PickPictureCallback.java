package com.ffomall.rns.almns;

import java.util.List;

/**
 */
public interface PickPictureCallback {
    void onStart();

    void onSuccess(List<PictureTotal> list);

    void onError();
}
