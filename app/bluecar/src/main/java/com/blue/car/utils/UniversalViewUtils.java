package com.blue.car.utils;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.blue.car.R;

public class UniversalViewUtils {

    public static View initNormalInfoLayout(Activity activity, int parentId, String leftText, String rightText) {
        ViewGroup viewGroup = (ViewGroup) activity.findViewById(parentId);
        TextView leftTextView = getLeftTextView(viewGroup);
        TextView rightTextView = getRightTextView(viewGroup);
        leftTextView.setText(leftText);
        rightTextView.setText(rightText);
        return rightTextView;
    }

    public static View initNormalInfoLayout(Activity activity, int parentId, String leftText, int rightImageResId) {
        ViewGroup viewGroup = (ViewGroup) activity.findViewById(parentId);
        TextView leftTextView = getLeftTextView(viewGroup);
        ImageView rightImageView = getRightImageView(viewGroup);
        leftTextView.setText(leftText);
        rightImageView.setImageResource(rightImageResId);
        return rightImageView;
    }

    public static View initNormalSwitchLayout(Activity activity, int parentId, String leftText) {
        ViewGroup viewGroup = (ViewGroup) activity.findViewById(parentId);
        TextView leftTextView = getLeftTextView(viewGroup);
        Switch rightSwitch = getSwitchView(viewGroup);
        leftTextView.setText(leftText);
        return rightSwitch;
    }

    public static View initNormalSeekBarLayout(Activity activity, int parentId, final String leftText,
                                               int seekProgress, final int offset, final SeekBar.OnSeekBarChangeListener listener) {
        ViewGroup viewGroup = (ViewGroup) activity.findViewById(parentId);
        TextView leftTextView = getLeftTextView(viewGroup);
        leftTextView.setText(leftText);
        final TextView rightTextView = getRightTextView(viewGroup);
        rightTextView.setText(String.valueOf(seekProgress - offset));
        SeekBar seekBar = getSeekBarView(viewGroup);
        seekBar.setProgress(seekProgress);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (listener != null) {
                    listener.onProgressChanged(seekBar, progress, fromUser);
                }
                rightTextView.setText(String.valueOf(progress - offset));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (listener != null) {
                    listener.onStartTrackingTouch(seekBar);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (listener != null) {
                    listener.onStopTrackingTouch(seekBar);
                }
            }
        });
        return rightTextView;
    }

    public static View initNormalSeekBarLayoutWithoutRightTextSet(Activity activity, int parentId, final String leftText,
                                               int seekProgress, final int offset, final SeekBar.OnSeekBarChangeListener listener) {
        ViewGroup viewGroup = (ViewGroup) activity.findViewById(parentId);
        TextView leftTextView = getLeftTextView(viewGroup);
        leftTextView.setText(leftText);
        final TextView rightTextView = getRightTextView(viewGroup);
        rightTextView.setText(String.valueOf(seekProgress - offset));
        SeekBar seekBar = getSeekBarView(viewGroup);
        seekBar.setProgress(seekProgress);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (listener != null) {
                    listener.onProgressChanged(seekBar, progress, fromUser);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (listener != null) {
                    listener.onStartTrackingTouch(seekBar);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (listener != null) {
                    listener.onStopTrackingTouch(seekBar);
                }
            }
        });
        return rightTextView;
    }

    public static View initNormalSeekBarLayout(Activity activity, int parentId, final String leftText,
                                               int seekProgress, final SeekBar.OnSeekBarChangeListener listener) {
        return initNormalSeekBarLayout(activity, parentId, leftText, seekProgress, 0, listener);
    }

    public static TextView getLeftTextView(ViewGroup viewGroup) {
        return (TextView) viewGroup.findViewById(R.id.info_left_text);
    }

    public static TextView getRightTextView(ViewGroup viewGroup) {
        return (TextView) viewGroup.findViewById(R.id.info_right_text);
    }

    public static ImageView getRightImageView(ViewGroup viewGroup) {
        return (ImageView) viewGroup.findViewById(R.id.info_right_image);
    }

    public static SeekBar getSeekBarView(ViewGroup viewGroup) {
        return (SeekBar) viewGroup.findViewById(R.id.info_seek_bar);
    }

    public static Switch getSwitchView(ViewGroup viewGroup) {
        return (Switch) viewGroup.findViewById(R.id.info_right_switch);
    }

    public static View getItemDividerView(ViewGroup viewGroup) {
        return viewGroup.findViewById(R.id.item_divider);
    }
}
