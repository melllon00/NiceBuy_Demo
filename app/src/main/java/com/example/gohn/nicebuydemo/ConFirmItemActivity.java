package com.example.gohn.nicebuydemo;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.*;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ConFirmItemActivity extends AppCompatActivity {
    private ValueCallback<Uri> filePathCallbackNormal;
    private ValueCallback<Uri[]> filePathCallbackLollipop;
    private final static int FILECHOOSER_NORMAL_REQ_CODE = 1;
    private final static int FILECHOOSER_LOLLIPOP_REQ_CODE = 2;
    // 웹뷰의 경우에는 input 을 할 때 제안이 있기 때문에 관련 소스코드를 추가해준다.

    private WebView webView;
    private String webView_URL;
    //웹뷰 변수랑 웹뷰 주소 설정할 변수 설정

    double backtime_first=0;
    double backtime_last;       // 뒤로가기 두 번 누르면 종료되는 버튼 기능 설정위해

    public static final int REQUEST_CONFIRMITEM_AND_MAIN = 1001;
    ValueCallback<Uri> _uploadMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_con_firm_item);
        Intent intent = getIntent();
        String barCode_Content = intent.getStringExtra("BarCode_Content");
        String barCode_Format = intent.getStringExtra("BarCode_Format");

        // 웹뷰 정의
        webView = (WebView) findViewById(R.id.webView_Confirm);
        WebSettings webSettings = webView.getSettings();
        //webSettings.setBuiltInZoomControls(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient(){}); // 새창 띄우지 않기 위해 필요한 소스 코드
        webView_URL = "http://nicebuy.0-w-0.com/apps/mobile/barcode/barcode_find.asp?pDeviceID=" + getDeviceSerialNumber() + "&pBarCode=" + barCode_Content;
        //Toast.makeText(getApplicationContext(), barCode_Content + " // " + barCode_Format + "\n기기번호 : " + getDeviceSerialNumber(), Toast.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(), "두 번 뒤로가기를 누르면 메인 페이지로 이동합니다." , Toast.LENGTH_LONG).show();
        webView.loadUrl(webView_URL);
        // 웹뷰 세팅한다. 웹뷰의 경우에는 input 을 할 때 제안이 있기 때문에 관련 소스코드를 추가해준다.
        webView.setWebChromeClient(new WebChromeClient(){
            // For Android 3.0+
            public void openFileChooser( ValueCallback<Uri> uploadMsg, String acceptType) {
                Log.d("MainActivity", "3.0+");
                filePathCallbackNormal = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_NORMAL_REQ_CODE);
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                Log.d("MainActivity", "4.1+");
                openFileChooser(uploadMsg, acceptType);
            }

            // For Android 5.0+
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    WebChromeClient.FileChooserParams fileChooserParams) {
                Log.d("MainActivity", "5.0+");
                if (filePathCallbackLollipop != null) {
                    filePathCallbackLollipop.onReceiveValue(null);
                    filePathCallbackLollipop = null;
                }
                filePathCallbackLollipop = filePathCallback;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_LOLLIPOP_REQ_CODE);

                return true;
            }
        });
    }

        // 웹뷰 세팅한다. 웹뷰의 경우에는 input 을 할 때 제안이 있기 때문에 관련 소스코드를 추가해준다. onActivityResult 를 Overide 해줘야한다.

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILECHOOSER_NORMAL_REQ_CODE) {
            if (filePathCallbackNormal == null) return ;
            Uri result = (data == null || resultCode != RESULT_OK) ? null : data.getData();
            filePathCallbackNormal.onReceiveValue(result);
            filePathCallbackNormal = null;
        } else if (requestCode == FILECHOOSER_LOLLIPOP_REQ_CODE) {
            if (filePathCallbackLollipop == null) return ;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                filePathCallbackLollipop.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            }
            filePathCallbackLollipop = null;
        }

        if(requestCode==49374) {            //49374 는 바코드 인식 zxing 후 주는 요청코드입니다.
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result.getContents() != null) {
                Intent intent_Confirm = new Intent(getApplicationContext(), ConFirmItemActivity.class);
                intent_Confirm.putExtra("BarCode_Content", result.getContents());
                intent_Confirm.putExtra("BarCode_Format", result.getFormatName());
                startActivityForResult(intent_Confirm, REQUEST_CONFIRMITEM_AND_MAIN);
                finish();
            }else{
                // http://helloms.kr/entry/Android%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-Zxing-%EC%82%AC%EC%9A%A9%ED%95%98%EA%B8%B0-%EC%84%B8%EB%A1%9C%EB%AA%A8%EB%93%9C    //
                // http://thegreedyman.tistory.com/archive/20150604
                Toast.makeText(getApplicationContext(), "인식이 되지 않았습니다.", Toast.LENGTH_LONG).show();
            }
        }
    }


    private static String getDeviceSerialNumber() {     //기기 고유 넘버를 받아오기 위한 메소드.
        try {
            return (String) Build.class.getField("SERIAL").get(null);
        } catch (Exception ignored) {
            return null;
        }
    }

    // 뒤로가기 버튼을 누르면 초를 계산해서 어플 종료할지 안할지 설정한다.
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if((keyCode == KeyEvent.KEYCODE_BACK)) {
            backtime_last = System.currentTimeMillis();
        }
        if(backtime_last-backtime_first<500) {
            if ((keyCode == KeyEvent.KEYCODE_BACK)) {
                finish();
            }
        }else{
            if ((keyCode == KeyEvent.KEYCODE_BACK)) {
                webView.goBack();
                backtime_first = System.currentTimeMillis();
                return true;
            }
            return super.onKeyDown(keyCode, event);
        }
        return false;
    }

    public void onButtonConfirm(View v){
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.addExtra("PROMPT_MESSAGE","상품코드를 사각형 안에 비춰주세요.");
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }
}
