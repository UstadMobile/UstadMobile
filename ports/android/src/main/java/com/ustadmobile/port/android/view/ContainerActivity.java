package com.ustadmobile.port.android.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.ocf.UstadOCF;
import com.ustadmobile.core.opf.UstadJSOPF;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;

public class ContainerActivity extends AppCompatActivity {

    private ContainerViewAndroid containerView;

    private int viewId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);


        viewId = getIntent().getIntExtra(UstadMobileSystemImplAndroid.EXTRA_VIEWID, 0);
        containerView = ContainerViewAndroid.getViewById(viewId);
        containerView.setContainerActivity(this);
        initByContentType();
    }

    public void onStart() {
        super.onStart();
        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityStart(this);
    }

    public void onStop() {
        super.onStop();
        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityStop(this);
    }

    public void onDestroy() {
        super.onDestroy();
        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityDestroy(this);
    }


    public void initByContentType() {
        if(containerView.getContainerController().getMimeType().startsWith("application/epub+zip")) {
            initEPUB();
        }
    }

    public void initEPUB() {
        UstadOCF ocf = null;
        String firstURL = null;

        try {
            ocf = containerView.getContainerController().getOCF();
            String opfPath = UMFileUtil.joinPaths(new String[]{
                    containerView.getContainerController().getOpenPath(), ocf.rootFiles[0].fullPath});
            UstadJSOPF opf = containerView.getContainerController().getOPF(0);
            firstURL = UMFileUtil.resolveLink(opfPath, opf.spine[1].href);
            WebView webView = (WebView)findViewById(R.id.container_webview);
            webView.loadUrl(firstURL);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_container, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
