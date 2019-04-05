package s1513911.gcu.s1513911_hons;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class appUniDepartments extends AppCompatActivity implements MessageDialogFragment.Listener {


    /*Resource caches
    private int mColorHearing;
    private int mColorNotHearing;

    private TextView mStatus;
    private TextView mText;

    private FloatingActionButton speechEnable;
    private CardView cardView;

*/
    private Toolbar mToolbar;
    private String getInfo;
    private TextView mTextView;

    private static final String FRAGMENT_MESSAGE_DIALOG = "message_dialog";


    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;

    private SpeechService mSpeechService;

    private VoiceRecorder mVoiceRecorder;
    private final VoiceRecorder.Callback mVoiceCallback = new VoiceRecorder.Callback() {

        @Override
        public void onVoiceStart() {

            if (mSpeechService != null) {
                mSpeechService.startRecognizing(mVoiceRecorder.getSampleRate());
            }
        }

        @Override
        public void onVoice(byte[] data, int size) {
            if (mSpeechService != null) {
                mSpeechService.recognize(data, size);
            }
        }

        @Override
        public void onVoiceEnd() {

            if (mSpeechService != null) {
                mSpeechService.finishRecognizing();
            }
        }

    };


    private boolean isEnabled = false;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mSpeechService = SpeechService.from(binder);
            mSpeechService.addListener(mSpeechServiceListener);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mSpeechService = null;
        }

    };



    private TextView mCompLink;
    private TextView mBusinessLink;
    private TextView mHealthLink;
    private boolean running = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_uni_departments);

        final Resources resources = getResources();
        final Resources.Theme theme = getTheme();


        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                stopVoiceRecorder();
            }
        });

        mCompLink = (TextView) findViewById(R.id.linkforCEBE);
        mCompLink.setMovementMethod(LinkMovementMethod.getInstance());

        mBusinessLink = (TextView) findViewById(R.id.linkforBusiness);
        mBusinessLink.setMovementMethod(LinkMovementMethod.getInstance());

        mHealthLink = (TextView) findViewById(R.id.linkforHealth);
        mHealthLink.setMovementMethod(LinkMovementMethod.getInstance());

    }

    ;


    @Override
    protected void onStart() {
        super.onStart();

        // Prepare Cloud Speech API
        bindService(new Intent(this, SpeechService.class), mServiceConnection, BIND_AUTO_CREATE);
        // Start listening to voices
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startVoiceRecorder();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.RECORD_AUDIO)) {
            showPermissionMessageDialog();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }

        MediaPlayer mp = MediaPlayer.create(appUniDepartments.this, R.raw.departments);
        mp.start();


    }

    @Override
    protected void onStop() {
        // Stop listening to voice
        stopVoiceRecorder();

        // Stop Cloud Speech API
        mSpeechService.removeListener(mSpeechServiceListener);
        unbindService(mServiceConnection);
        mSpeechService = null;

        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (permissions.length == 1 && grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecorder();
            } else {
                showPermissionMessageDialog();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void startVoiceRecorder() {
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
        }
        mVoiceRecorder = new VoiceRecorder(mVoiceCallback);
        mVoiceRecorder.start();
    }

    private void stopVoiceRecorder() {
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
            mVoiceRecorder = null;
        }
    }

    private void showPermissionMessageDialog() {
        MessageDialogFragment
                .newInstance(getString(R.string.permission_message))
                .show(getSupportFragmentManager(), FRAGMENT_MESSAGE_DIALOG);
    }


    @Override
    public void onMessageDialogDismissed() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_RECORD_AUDIO_PERMISSION);
    }

    public void openLink(String string){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(string));
        startActivity(browserIntent);
    }

    public void readText(){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  //This is a long reading which prevents the device from going into standby
        stopVoiceRecorder();
        MediaPlayer mp = MediaPlayer.create(appUniDepartments.this, R.raw.departmentsreading);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                startVoiceRecorder();
            }

        });
        mp.start();
    }


    private final SpeechService.Listener mSpeechServiceListener =
            new SpeechService.Listener() {
                @Override
                public void onSpeechRecognized(final String text, final boolean isFinal) {
                    if (isFinal) {
                        mVoiceRecorder.dismiss();
                    }

                    runOnUiThread(new Runnable() { //Speech service runs in the background which requires to be brought to the UI thread
                        @Override
                        public void run() {
                            if (isFinal) {


                                String speechResult = text;

                                MediaPlayer mp = MediaPlayer.create(appUniDepartments.this, R.raw.enabledshort);
                                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                                    @Override
                                    public void onCompletion(MediaPlayer mp) {
                                        startVoiceRecorder();
                                    }

                                });


                                if(speechResult.equals("voice assist")){
                                    running = true;
                                    stopVoiceRecorder();
                                    mp.start();
                                }


                                if(speechResult.equals("read this") && running){
                                    readText();
                                    Toast.makeText(getApplicationContext(), "reading", Toast.LENGTH_SHORT).show();
                                }

                                if(speechResult.equals("back") && running){
                                    stopVoiceRecorder();
                                    MediaPlayer mpBack = MediaPlayer.create(appUniDepartments.this, R.raw.back);
                                    mpBack.start();
                                    finish();
                                }

                                if(speechResult.equals("stop") && running){
                                    running = false;
                                    stopVoiceRecorder();
                                    MediaPlayer mpStop = MediaPlayer.create(appUniDepartments.this, R.raw.stopping);
                                    mpStop.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                                        @Override
                                        public void onCompletion(MediaPlayer mp) {
                                            startVoiceRecorder();
                                        }

                                    });
                                    mpStop.start();
                                }

                                if(speechResult.contains("open") && running){
                                    if(speechResult.contains("computing") || speechResult.contains("engineering") || speechResult.contains("built environment")){
                                        openLink("https://www.gcu.ac.uk/cebe/aboutus/welcome/");
                                    }

                                    if(speechResult.contains("business") || speechResult.contains("society")){
                                        openLink("https://www.gcu.ac.uk/gsbs/");
                                    }

                                    if(speechResult.contains("health") || speechResult.contains("life sciences")){
                                        openLink("https://www.gcu.ac.uk/hls/");
                                    }

                                }


                            }
                        }
                    });
                }
            };
}
