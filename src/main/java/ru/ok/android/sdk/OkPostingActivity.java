package ru.ok.android.sdk;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.webkit.WebView;

public class OkPostingActivity extends AbstractWidgetActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ok_posting_activity);
        prepareWebView();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            args.put("st.attachment", bundle.getString(Shared.PARAM_ATTACHMENT));
            args.put("st.utext", bundle.getBoolean(Shared.PARAM_USER_TEXT_ENABLE, false) ? "on" : "off");
        }

        loadPage();
    }

    private void prepareWebView() {
        WebView webView = (WebView) findViewById(R.id.web_view);
        webView.setWebViewClient(new OkWidgetViewClient(this));
        webView.getSettings().setJavaScriptEnabled(true);
    }

    @Override
    protected int getCancelledMessageId() {
        return R.string.posting_canceled;
    }

    private void loadPage() {
        String url = prepareUrl(null);
        ((WebView) findViewById(R.id.web_view)).loadUrl(url);
    }

    @Override
    protected String getWidgetId() {
        return "WidgetMediatopicPost";
    }

    @Override
    protected void processError(final String error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(error);
        builder.setPositiveButton(getString(R.string.retry), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                loadPage();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                processResult(error);
            }
        });
        builder.show();
    }

    @Override
    protected void processResult(String result) {
        Odnoklassniki odnoklassniki = Odnoklassniki.getInstance();
        if (odnoklassniki != null) {
            try {
                JSONObject json = new JSONObject(result);
                String type = json.getString(Shared.PARAM_TYPE);
                if ("error".equals(type)) {
                    odnoklassniki.notifyFailed(json.getString(Shared.PARAM_MESSAGE));
                } else {
                    odnoklassniki.notifySuccess(json);
                }
            } catch (JSONException e) {
                odnoklassniki.notifyFailed(result);
            }
        }
        finish();
    }
}
