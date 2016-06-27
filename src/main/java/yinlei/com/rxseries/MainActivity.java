package yinlei.com.rxseries;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private NineRectangleGrid mLockPatternView;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLockPatternView = (NineRectangleGrid) findViewById(R.id.activity_main_lock);
        mTextView = (TextView) findViewById(R.id.activity_main_lock_me);

        mLockPatternView.setOnPatterChangeListener(new NineRectangleGrid.OnPatterChangeListener() {
            @Override
            public void onPatterChange(String passwordStr) {
                if (!TextUtils.isEmpty(passwordStr)){
                    mTextView.setText(passwordStr);
                }else {
                    mTextView.setText("至少绘制5个图案");
                }
            }

            @Override
            public void onPatterStart(boolean isStart) {
                if (isStart){
                    mTextView.setText("请绘制图案");
                }
                }
        });
    }
}
