package life;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import life.Jama.Polyfit;
import life.Jama.Polyval;
import life.orient.OrientSensor;
import life.step.StepSensorAcceleration;
import life.step.StepSensorBase;
import life.util.SensorUtil;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements StepSensorBase.StepCallBack, OrientSensor.OrientCallBack {
    private TextView mStepText;
    private TextView mOrientText;
    private StepView mStepView;

    private StepSensorBase mStepSensor; // 计步传感器
    private OrientSensor mOrientSensor; // 方向传感器
    private int mStepLen = 50; // 步长
    private List<ScanResult> results=new ArrayList<>();
    private ArrayList<String> arrayList = new ArrayList<>();
    private WifiManager wifiManager;
    private ArrayAdapter adapter;
    private static final String RC_LOCATION = "1";

    private static Map<String,Double> resultmap=new HashMap<>();
    @Override
    public void Step(int len) {
        //  计步回调
//        mStepText.setText("步数:" + stepNum);
        mStepView.autoAddPoint(mStepLen,resultmap);

    }
    public void myStep() {
        //  计步回调
//        mStepText.setText("步数:" + stepNum);
        mStepView.autoAddPoint(mStepLen,resultmap);

    }

    public void Orient(int orient) {
        // 方向回调
        mOrientText.setText("方向:" + orient);
//        获取手机转动停止后的方向
//        orient = SensorUtil.getInstance().getRotateEndOrient(orient);
        mStepView.autoDrawArrow(orient);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SensorUtil.getInstance().printAllSensor(this); // 打印所有可用传感器
        setContentView(R.layout.activity_main);
        mStepText = (TextView) findViewById(R.id.step_text);
        mOrientText = (TextView) findViewById(R.id.orient_text);
        mStepView = (StepView) findViewById(R.id.step_surfaceView);
        // 注册计步监听
//        mStepSensor = new StepSensorPedometer(this, this);
//        if (!mStepSensor.registerStep()) {
        mStepSensor = new StepSensorAcceleration(this, this);
        if (!mStepSensor.registerStep()) {
            Toast.makeText(this, "计步功能不可用！", Toast.LENGTH_SHORT).show();
        }
//        }
        // 注册方向监听
        mOrientSensor = new OrientSensor(this, this);
        if (!mOrientSensor.registerOrient()) {
            Toast.makeText(this, "方向功能不可用！", Toast.LENGTH_SHORT).show();
        }
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    scanSuccess();
                } else {
                    // scan failure handling
                    scanFailure();
                }
                Timer timer = new Timer(true);

                TimerTask task = new TimerTask() {
                    public void run() {
                        doscan();//每次需要执行的代码放到这里面。
                    }
                };
                timer.schedule(task,500,4000);
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);

        String location = android.Manifest.permission.ACCESS_COARSE_LOCATION;
        if (ActivityCompat.checkSelfPermission(this, location) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,new String[] { ACCESS_FINE_LOCATION }, 1);

        }else {
            doscan();
        }


    }
    public void doscan(){
        boolean success = wifiManager.startScan();
        List<ScanResult> list=wifiManager.getScanResults();

        if (!success) {
            // scan failure handling
            scanFailure();
        }else{
            System.out.println("RESULT");
            for(ScanResult sc:list){
                System.out.println(sc.toString());
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        System.out.println("granting");

        if (requestCode == 1) {
            System.out.println(results[0]);
            if (results[0] == PackageManager.PERMISSION_GRANTED) {
                boolean success = wifiManager.startScan();
                if (!success) {
                    // scan failure handling
                    scanFailure();
                }
            } else {
                // user rejected permission request
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, results);
        }
    }
    private void scanSuccess() {
        results = wifiManager.getScanResults();
        int count=0;
        HashMap<String,Double> map=new HashMap<>();
        for(ScanResult sc:results){
            if(count>8){
                break;
            }
            map.put(sc.SSID,(double)sc.level);
            count++;

        }
        this.resultmap=map;
        System.out.println(results.size());
        System.out.println("LOG:SCAN_SUCCESS");
    }


    private void scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        results = wifiManager.getScanResults();
        System.out.println("LOG:SCAN_FAILURE");
    }

    public void dobegin(View view){
        this.resultmap=new HashMap<>();
    }
    public void dorecord(View v){
        myStep();
    }
    public void dodraw(View v){
        mStepView.drawcircle();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销传感器监听
        mStepSensor.unregisterStep();
        mOrientSensor.unregisterOrient();
    }
}
