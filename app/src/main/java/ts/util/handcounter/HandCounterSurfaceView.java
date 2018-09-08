package ts.util.handcounter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

class HandCounterSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    Context context;
    SurfaceHolder mHolder;
    DrawThread mThread;

    //Bitmap Object Announcement
    Bitmap Back;
    Bitmap number;
    Bitmap mute_on;
    Bitmap mute_off;
    Bitmap vibrate_on;
    Bitmap vibrate_off;
    Bitmap resetbutton;
    Bitmap undobutton;
    Bitmap settingbutton;

    public SoundPool music;
    private int stream;

    //
    int screenWidth, screenHeight;

    public HandCounterSurfaceView(Context context) {
        super(context);

        this.context= context;

        Back = BitmapFactory.decodeResource(context.getResources(), R.drawable.back);
        number = BitmapFactory.decodeResource(context.getResources(), R.drawable.number);
        mute_on = BitmapFactory.decodeResource(context.getResources(), R.drawable.mute1);
        mute_off = BitmapFactory.decodeResource(context.getResources(), R.drawable.mute0);
        vibrate_on = BitmapFactory.decodeResource(context.getResources(), R.drawable.vibrate0);
        vibrate_off = BitmapFactory.decodeResource(context.getResources(), R.drawable.vibrate1);
        resetbutton = BitmapFactory.decodeResource(context.getResources(), R.drawable.resetbutton);
        undobutton = BitmapFactory.decodeResource(context.getResources(), R.drawable.undobutton);
        settingbutton = BitmapFactory.decodeResource(context.getResources(), R.drawable.settingbutton);

        mHolder = getHolder();
        mHolder.addCallback(this);

        music = new SoundPool(1, AudioManager.STREAM_MUSIC,0);
        music.setOnLoadCompleteListener(mListener);
    }

    SoundPool.OnLoadCompleteListener mListener = new SoundPool.OnLoadCompleteListener() {
        public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
            if(status==0)
            {
                stream = soundPool.play(sampleId, 1, 1, 0, 0, 1);
            }
        }
    };

    public void surfaceCreated(SurfaceHolder holder) {
        mThread = new DrawThread(mHolder);
        mThread.start();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        mThread.bExit = true;
        for (;;) {
            try {
                mThread.join();
                break;
            }
            catch (Exception e) {;}
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mThread != null) {
            mThread.SizeChange(width, height);
        }
    }

    public boolean onTouchEvent(MotionEvent event){

        if(event.getAction() == MotionEvent.ACTION_DOWN){

            if(rect_reset.contains((int)event.getX(), (int)event.getY())){
                MainActivity.countint=0;
                music.load(context, R.raw.reset, 1);
            }
            else if(rect_undo.contains((int)event.getX(), (int)event.getY())){
                MainActivity.countint--;
                if(MainActivity.countint < 0)
                    MainActivity.countint=0;
                music.load(context, R.raw.undo, 1);

                if(MainActivity.isVibrate) MainActivity.vibrator.vibrate(MainActivity.vibrateTime * 2);
            }
            else if(rect_setting.contains((int)event.getX(), (int)event.getY())){
                final LinearLayout linear = (LinearLayout) View.inflate(context, ts.util.handcounter.R.layout.input, null);

                final EditText editText =(EditText)linear.findViewById(R.id.editText1);

                new AlertDialog.Builder(context)
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
                                    Toast toast = Toast.makeText(context, "숫자가 입력되지 않아 취소됩니다.", Toast.LENGTH_SHORT);
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
            else if(rect_mute.contains((int)event.getX(), (int)event.getY())){
                MainActivity.mute = !MainActivity.mute;
            }
            else if(rect_vibrate.contains((int)event.getX(), (int)event.getY())){
                MainActivity.isVibrate = !MainActivity.isVibrate;
            }
            else{
                MainActivity.countint++;
                if(MainActivity.countint > 9999)
                    MainActivity.countint=0;

                if(!MainActivity.mute){
                    music.load(context, R.raw.count, 1);
                }

                if(MainActivity.isVibrate) MainActivity.vibrator.vibrate(MainActivity.vibrateTime);
            }
            return true;
        }
        return false;
    }

    //Render
    Rect rect_back;
    Rect rect_mute;
    Rect rect_vibrate;
    Rect rect_reset;
    Rect rect_undo;
    Rect rect_setting;

    //
    final float rate_mute_top = 0.028f;
    final float rate_mute_left = 0.398f;
    final float rate_mute_width = 0.087f;
    final float rate_mute_height = 0.043f;

    final float rate_vibrate_left = 0.515f;
    final float rate_vibrate_width = 0.087f;
    final float rate_vibrate_height = 0.049f;

    final float rate_button_top = 0.088f;
    final float rate_button_width = 0.207f;
    final float rate_button_height = 0.053f;

    final float rate_reset_button_left = 0.166f;
    final float rate_undo_button_left = 0.401f;
    final float rate_setting_button_left = 0.637f;

    class DrawThread extends Thread {
        boolean bExit;
        Paint P = new Paint();

        SurfaceHolder mHolder;

        DrawThread(SurfaceHolder Holder) {
            mHolder = Holder;
            bExit = false;

            P.setStyle(Paint.Style.FILL);
            P.setColor(Color.RED);

        }

        public void SizeChange(int Width, int Height) {
            screenWidth = Width;
            screenHeight = Height;

            int back_top = (screenHeight - screenWidth) /2;
            rect_back = new Rect(0, back_top, screenWidth , back_top + screenWidth);

            int centerX = (int)screenWidth / 2;

            rect_mute = new Rect();
            rect_mute.left = (int)(rate_mute_left * screenWidth);
            rect_mute.top = (int)(rect_back.top + (rate_mute_top * screenHeight));
            rect_mute.right = (int)(rect_mute.left + rate_mute_width * screenWidth);
            rect_mute.bottom = (int)(rect_mute.top + rate_mute_height * screenHeight);

            rect_vibrate = new Rect();
            rect_vibrate.left = (int)(rate_vibrate_left * screenWidth);
            rect_vibrate.top = (int)(rect_back.top + (rate_mute_top * screenHeight));
            rect_vibrate.right = (int)(rect_vibrate.left + rate_vibrate_width * screenWidth);
            rect_vibrate.bottom = (int)(rect_vibrate.top + rate_vibrate_height * screenHeight);

            rect_reset = new Rect();
            rect_reset.left = (int)(rate_reset_button_left * screenWidth);
            rect_reset.top = (int)(rate_button_top * screenHeight) + back_top;
            rect_reset.right = rect_reset.left + (int)(rate_button_width * screenWidth);
            rect_reset.bottom = rect_reset.top + (int)(rate_button_height * screenHeight);

            rect_undo = new Rect();
            rect_undo.left = (int)(rate_undo_button_left * screenWidth);
            rect_undo.top = (int)(rate_button_top * screenHeight) + back_top;
            rect_undo.right = rect_undo.left + (int)(rate_button_width * screenWidth);
            rect_undo.bottom = rect_undo.top + (int)(rate_button_height * screenHeight);

            rect_setting = new Rect();
            rect_setting.left = (int)(rate_setting_button_left * screenWidth);
            rect_setting.top = (int)(rate_button_top * screenHeight) + back_top;
            rect_setting.right = rect_setting.left + (int)(rate_button_width * screenWidth);
            rect_setting.bottom = rect_setting.top + (int)(rate_button_height * screenHeight);

        }

        public void run() {
            Canvas canvas;

            while (bExit == false) {

                int countint = MainActivity.countint;

                synchronized(mHolder) {
                    canvas = mHolder.lockCanvas();
                    if (canvas == null) break;
                    canvas.drawColor(Color.BLACK);

                    canvas.drawBitmap(Back,null,rect_back, null);

                    int nw = number.getWidth()/10;
                    int nh = number.getHeight();

                    canvas.drawBitmap(number,getsrcRect(getint(countint,1000)*nw, 0, nw, nh),getdstRect(0), null);
                    canvas.drawBitmap(number,getsrcRect(getint(countint,100)*nw, 0, nw, nh),getdstRect(1), null);
                    canvas.drawBitmap(number,getsrcRect(getint(countint,10)*nw, 0, nw, nh),getdstRect(2), null);
                    canvas.drawBitmap(number,getsrcRect(getint(countint,1)*nw, 0, nw, nh),getdstRect(3), null);

                    if(MainActivity.mute){
                        canvas.drawBitmap(mute_on,null,rect_mute, null);
                    }else{
                        canvas.drawBitmap(mute_off,null,rect_mute, null);
                    }

                    if(MainActivity.isVibrate){
                        canvas.drawBitmap(vibrate_on,null,rect_vibrate, null);
                    }else{
                        canvas.drawBitmap(vibrate_off,null,rect_vibrate, null);
                    }

                    canvas.drawBitmap(resetbutton,null,rect_reset, null);
                    canvas.drawBitmap(undobutton,null,rect_undo, null);
                    canvas.drawBitmap(settingbutton,null,rect_setting, null);

                    mHolder.unlockCanvasAndPost(canvas);
                }

                try { Thread.sleep(50); } catch (Exception e) {;}
            }
        }

        private int getint(int countint, int i) {
            return (int)(countint/i) - (int)(countint/(i*10)*10);
        }

        public Rect getsrcRect(int srcX, int srcY,int srcWidth, int srcHeight){
            Rect srcRect = new Rect();
            srcRect.left = srcX;
            srcRect.top = srcY;
            srcRect.right = srcX + srcWidth - 1;
            srcRect.bottom = srcY + srcHeight - 1;
            return srcRect;
        }

        final float num_top = 0.344f;
        final float num_left = 0.088f;
        final float num_width = 0.191f;
        final float num_padding =0.02f;
        final float num_height = 0.34f;

        public Rect getdstRect(int x){
            Rect dstRect = new Rect();
            dstRect.left = (int)((num_left * screenWidth) + ((num_width + num_padding) * x * screenWidth));
            dstRect.top = (int) (num_top*screenWidth) + rect_back.top;
            dstRect.right = dstRect.left+(int)(num_width*screenWidth);
            dstRect.bottom = dstRect.top + (int)(num_height*screenWidth);
            return dstRect;
        }

    }


}
