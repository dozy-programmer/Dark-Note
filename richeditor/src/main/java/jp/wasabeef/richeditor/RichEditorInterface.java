package jp.wasabeef.richeditor;

import android.webkit.JavascriptInterface;

public class RichEditorInterface {

    private final RichEditor richEditor;

    RichEditorInterface(RichEditor richEditor) {
        this.richEditor = richEditor;
    }

    @JavascriptInterface
    public void getImagePath(String imagePath) {
        richEditor.setImagePath(imagePath);
    }

    @JavascriptInterface
    public void getImagePath(String imagePath, String html, int width, int height) {
        richEditor.setImagePath(imagePath, html, width, height);
    }

    @JavascriptInterface
    public void getCursorPositionY(int position) {
        richEditor.setYPosition(position);
    }

}
