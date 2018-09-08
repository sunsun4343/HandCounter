package ts.util.handcounter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.kakao.adfit.ads.AdListener;
import com.kakao.adfit.ads.ba.BannerAdView;

public class MainActivity extends AppCompatActivity {

    private static final String LOGTAG = "BannerTypeJava";
    private RelativeLayout relativeLayout = null;
    private BannerAdView adView = null;

    HandCounterSurfaceView vw;
    PowerManager.WakeLock wakeLock;
    public static Vibrator vibrator;
    public static int vibrateTime = 25;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Init_Config();

        initAdFit();

        Init_Layout();

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "MainActivity");
        wakeLock.acquire();

        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);


    }

    public static int countint;
    public static boolean mute;
    public static boolean isVibrate;

    void Init_Config(){
        SharedPreferences pref = getSharedPreferences("PrefTest", 0);
        countint =pref.getInt("Countint", 0 );
        mute =pref.getBoolean("Mute", false);

    }

    void initAdFit(){
        // AdFit 광고 뷰 생성 및 설정
        adView = new BannerAdView(this);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Log.d(LOGTAG, "onAdLoaded");
            }

            @Override
            public void onAdFailed(int code) {
                Log.d(LOGTAG, "onAdFailed " + code);
            }

            @Override
            public void onAdClicked() {
                Log.d(LOGTAG, "onAdClicked");
            }
        });

        // 할당 받은 clientId 설정
        adView.setClientId("DAN-qdqhj88gdyy8");

        // 광고 갱신 시간 : 기본 60초
        // 0 으로 설정할 경우, 갱신하지 않음.
        adView.setRequestInterval(30);

        // 광고 사이즈 설정
        adView.setAdUnitSize("320x50");

        // 광고 불러오기
        adView.loadAd();
    }

    void Init_Layout(){
        FrameLayout BackForm = new FrameLayout(this);
        LinearLayout LyAdd = new LinearLayout(this);

        vw = new HandCounterSurfaceView(this);
        BackForm.addView(vw);

        LyAdd.addView(adView, LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LyAdd.setGravity(Gravity.BOTTOM);

        BackForm.addView(LyAdd);

        setContentView(BackForm);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getTitle().equals("Reset")){
            MainActivity.countint=0;
        }else if(item.getTitle().equals("Undo")){
            MainActivity.countint--;
            if(MainActivity.countint < 0)
                MainActivity.countint=0;
        }else{
            final LinearLayout linear = (LinearLayout) View.inflate(MainActivity.this, ts.util.handcounter.R.layout.input, null);

            final EditText editText =(EditText)linear.findViewById(R.id.editText1);

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("set")
                    .setView(linear)
                    .setPositiveButton("set",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try
                            {
                                int num = Integer.parseInt(editText.getText().toString());

                                if(num > 9999){
                                    num = 9999;
                                }

                                MainActivity.countint = num;

                            }catch (NumberFormatException ex){
                                Toast toast = Toast.makeText(getApplicationContext(), "숫자가 입력되지 않아 취소됩니다.", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        }
        return false;
    }

    @Override
    public void onResume(){
        super.onResume();
        wakeLock.acquire();
        if (adView != null) {
            adView.resume();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        wakeLock.release();
        if (adView != null) {
            adView.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        SharedPreferences pref = getSharedPreferences("PrefTest",0);
        SharedPreferences.Editor edit = pref.edit();

        edit.putInt("Countint", MainActivity.countint);
        edit.putBoolean("Mute", MainActivity.mute);

        edit.commit();

        if (adView != null) {
            adView.destroy();
            adView = null;
        }
    }

    public boolean onKeyDown(int keycode, KeyEvent event)
    {
        switch(keycode)
        {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                MainActivity.countint--;
                if(MainActivity.countint < 0)
                    MainActivity.countint=0;

                vw.music.load(this, R.raw.undo, 1);

                if(isVibrate) vibrator.vibrate(vibrateTime* 2);

                break;

            case KeyEvent.KEYCODE_VOLUME_UP:
                MainActivity.countint++;
                if(MainActivity.countint > 9999)
                    MainActivity.countint=0;

                vw.music.load(this, R.raw.count, 1);

                if(isVibrate) vibrator.vibrate(vibrateTime);

            break;
        }
        return true;
    }



}
