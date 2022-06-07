package com.feipai.flypai.beans;

import android.util.ArrayMap;

import com.feipai.flypai.R;
import com.feipai.flypai.base.BaseEntity;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.ResourceUtils;

import java.util.List;
import java.util.Map;

public class SettingBean extends BaseEntity {
    /**
     * rval : 0
     * msg_id : 3
     * param : [{"video_quality":"sfine"},{"video_resolution":"1920x1080 60P 16:9"},{"default_setting":"n/a"},{"camera_clock":"2017-12-19 18:54:18"},{"photo_size":"16M (4608x3456 4:3)"},{"frequency":"auto"},{"video_wb":"auto"},{"video_color":"off"},{"video_ev":"0"},{"video_meter":"center"},{"video_contrast":"0"},{"photo_wb":"auto"},{"photo_color":"off"},{"photo_ev":"0"},{"photo_meter":"center"},{"photo_contrast":"0"},{"photo_iso":"auto"},{"photo_shutter":"auto"},{"system_type":"ntsc"},{"version":"FP-V1.0-20171213"},{"photo_vr_mode":"off"},{"photo_raw":"off"},{"video_zoom":"off"},{"photo_zoom":"off"},{"video_image_mode":"manual"},{"photo_image_mode":"manual"},{"video_saturation":"0"},{"photo_saturation":"0"},{"video_sharpness":"std"},{"photo_sharpness":"std"}]
     */

    private int rval;
    private int msg_id;
    private List<ParamBean> param;

    public int getRval() {
        return rval;
    }

    public void setRval(int rval) {
        this.rval = rval;
    }

    public int getMsg_id() {
        return msg_id;
    }

    public void setMsg_id(int msg_id) {
        this.msg_id = msg_id;
    }

    public List<ParamBean> getParam() {
        return param;
    }

    public void setParam(List<ParamBean> param) {
        this.param = param;
    }


    public ParamBean getSettingParamBean() {
        ParamBean paramBean = new ParamBean();
        Map<String, String> map = new ArrayMap<>();
        for (ParamBean bean : getParam()) {
            if (bean.video_quality != null && !bean.video_quality.equals("")) {
                paramBean.setVideo_quality(bean.video_quality);
            } else if (bean.video_resolution != null && !bean.video_resolution.equals("")) {
                paramBean.setVideo_resolution(bean.video_resolution);
            } else if (bean.default_setting != null && !bean.default_setting.equals("")) {
                paramBean.setDefault_setting(bean.default_setting);
            } else if (bean.camera_clock != null && !bean.camera_clock.equals("")) {
                paramBean.setCamera_clock(bean.camera_clock);
            } else if (bean.photo_size != null && !bean.photo_size.equals("")) {
                paramBean.setPhoto_size(bean.photo_size);
            } else if (bean.frequency != null && !bean.frequency.equals("")) {
                paramBean.setFrequency(bean.frequency);
            } else if (bean.video_wb != null && !bean.video_wb.equals("")) {
                paramBean.setVideo_wb(bean.video_wb);
            } else if (bean.video_color != null && !bean.video_color.equals("")) {
                paramBean.setVideo_color(bean.video_color);
            } else if (bean.video_ev != null && !bean.video_ev.equals("")) {
                paramBean.setVideo_ev(bean.video_ev);
            } else if (bean.video_meter != null && !bean.video_meter.equals("")) {
                paramBean.setVideo_meter(bean.video_meter);
            } else if (bean.video_contrast != null && !bean.video_contrast.equals("")) {
                paramBean.setVideo_contrast(bean.video_contrast);
            } else if (bean.photo_wb != null && !bean.photo_wb.equals("")) {
                paramBean.setPhoto_wb(bean.photo_wb);
            } else if (bean.photo_color != null && !bean.photo_color.equals("")) {
                paramBean.setPhoto_color(bean.photo_color);
            } else if (bean.photo_ev != null && !bean.photo_ev.equals("")) {
                paramBean.setPhoto_ev(bean.photo_ev);
            } else if (bean.photo_meter != null && !bean.photo_meter.equals("")) {
                paramBean.setPhoto_meter(bean.photo_meter);
            } else if (bean.photo_contrast != null && !bean.photo_contrast.equals("")) {
                paramBean.setPhoto_contrast(bean.photo_contrast);
            } else if (bean.photo_iso != null && !bean.photo_iso.equals("")) {
                paramBean.setPhoto_iso(bean.photo_iso);
            } else if (bean.photo_shutter != null && !bean.photo_shutter.equals("")) {
                paramBean.setPhoto_shutter(bean.photo_shutter);
            } else if (bean.video_shutter != null && !bean.video_shutter.equals("")) {
                paramBean.setVideo_shutter(bean.video_shutter);
            } else if (bean.video_iso != null && !bean.video_iso.equals("")) {
                paramBean.setVideo_iso(bean.video_iso);
            } else if (bean.system_type != null && !bean.system_type.equals("")) {
                paramBean.setSystem_type(bean.system_type);
            } else if (bean.version != null && !bean.version.equals("")) {
                paramBean.setVersion(bean.version);
            } else if (bean.photo_vr_mode != null && !bean.photo_vr_mode.equals("")) {
                paramBean.setPhoto_vr_mode(bean.photo_vr_mode);
            } else if (bean.photo_time_lapse_mode != null && !bean.photo_time_lapse_mode.equals("")) {
                paramBean.setPhoto_time_lapse_mode(bean.photo_time_lapse_mode);
            } else if (bean.photo_raw != null && !bean.photo_raw.equals("")) {
                paramBean.setPhoto_raw(bean.photo_raw);
            } else if (bean.video_zoom != null && !bean.video_zoom.equals("")) {
                paramBean.setVideo_zoom(bean.video_zoom);
            } else if (bean.photo_zoom != null && !bean.photo_zoom.equals("")) {
                paramBean.setPhoto_zoom(bean.photo_zoom);
            } else if (bean.video_image_mode != null && !bean.video_image_mode.equals("")) {
                paramBean.setVideo_image_mode(bean.video_image_mode);
            } else if (bean.photo_image_mode != null && !bean.photo_image_mode.equals("")) {
                paramBean.setPhoto_image_mode(bean.photo_image_mode);
            } else if (bean.video_saturation != null && !bean.video_saturation.equals("")) {
                paramBean.setVideo_saturation(bean.video_saturation);
            } else if (bean.photo_saturation != null && !bean.photo_saturation.equals("")) {
                paramBean.setPhoto_saturation(bean.photo_saturation);
            } else if (bean.video_sharpness != null && !bean.video_sharpness.equals("")) {
                paramBean.setVideo_sharpness(bean.video_sharpness);
            } else if (bean.photo_sharpness != null && !bean.photo_sharpness.equals("")) {
                paramBean.setPhoto_sharpness(bean.photo_sharpness);
            } else if (bean.video_3a_lock != null && !bean.video_3a_lock.equals("")) {
//                MLog.log("设置视频曝光锁定" + bean.video_3a_lock);
                paramBean.setVideo_3a_lock(bean.video_3a_lock);
            } else if (bean.photo_3a_lock != null && !bean.photo_3a_lock.equals("")) {
//                MLog.log("设置拍照曝光锁定" + bean.photo_3a_lock);
                paramBean.setPhoto_3a_lock(bean.photo_3a_lock);
            } else if (bean.free_size != null && !bean.free_size.equals("")) {
                paramBean.setFree_size(bean.free_size);
            } else if (bean.total_size != null && !bean.total_size.equals("")) {
                paramBean.setTotal_size(bean.total_size);
            } else if (bean.video_10bit_mode != null && !bean.video_10bit_mode.equals("")) {
                paramBean.setVideo_10bit_mode(bean.video_10bit_mode);
            } else if (bean.fen_mode != null && !bean.fen_mode.equals("")) {
                paramBean.setFen_mode(bean.fen_mode);
            }
        }
        return paramBean;
    }

    public static class ParamBean {
        /**
         * video_quality : sfine
         * video_resolution : 1920x1080 60P 16:9
         * default_setting : n/a
         * camera_clock : 2017-12-19 18:54:18
         * photo_size : 16M (4608x3456 4:3)
         * frequency : auto
         * video_wb : auto
         * video_color : off
         * video_ev : 0
         * video_meter : center
         * video_contrast : 0
         * photo_wb : auto
         * photo_color : off
         * photo_ev : 0
         * photo_meter : center
         * photo_contrast : 0
         * photo_iso : auto
         * photo_shutter : auto
         * system_type : ntsc
         * version : FP-V1.0-20171213
         * photo_vr_mode : off
         * photo_raw : off
         * video_zoom : off
         * photo_zoom : off
         * video_image_mode : manual
         * photo_image_mode : manual
         * video_saturation : 0
         * photo_saturation : 0
         * video_sharpness : std
         * photo_sharpness : std
         */

        private String video_quality = "";
        private String video_resolution = "";
        private String default_setting = "";
        private String camera_clock = "";
        private String photo_size = "";
        private String frequency = "";
        private String video_wb = "";
        private String video_color = "";
        private String video_ev = "";
        private String video_meter = "";
        private String video_contrast = "";
        private String photo_wb = "";
        private String photo_color = "";
        private String photo_ev = "";
        private String photo_meter = "";
        private String photo_contrast = "";
        private String photo_iso = "";
        private String video_iso = "";
        private String photo_shutter = "";
        private String video_shutter = "";
        private String system_type = "";
        private String version = "";
        private String photo_vr_mode = "";
        private String video_10bit_mode = "";   // 6k才有
        private String fen_mode = "";   // 6k才有 风扇
        private String photo_time_lapse_mode = "";
        private String photo_raw = "";
        private String video_zoom = "";
        private String photo_zoom = "";
        private String video_image_mode = "";
        private String photo_image_mode = "";
        private String video_saturation = "";
        private String photo_saturation = "";
        private String video_sharpness = "";
        private String photo_sharpness = "";
        private String free_size = "0KB";
        private String total_size = "0KB";
        private String video_3a_lock = "";
        private String photo_3a_lock = "";

        public String getVideo_3a_lock() {
            return video_3a_lock;
        }

        public void setVideo_3a_lock(String video_3a_lock) {
            this.video_3a_lock = video_3a_lock;
        }

        public void setVideo_3a_lock(boolean video_3a_lock) {
            this.video_3a_lock = video_3a_lock ? "on" : "off";
        }

        public String getPhoto_3a_lock() {
            return photo_3a_lock;
        }

        public void setPhoto_3a_lock(String photo_3a_lock) {
            this.photo_3a_lock = photo_3a_lock;
        }

        public void setPhoto_3a_lock(boolean photo_3a_lock) {
            this.photo_3a_lock = photo_3a_lock ? "on" : "off";
        }

        public String getVideo_quality() {
            return video_quality;
        }

        public void setVideo_quality(String video_quality) {
            this.video_quality = video_quality;
        }

        public String getVideo_resolution() {
            return video_resolution;
        }

        public void setVideo_resolution(String video_resolution) {
            this.video_resolution = video_resolution;
        }

        public boolean getVideoShutter30Max() {
            if (video_resolution.contains("60P")) {
                return false;
            }
            return true;
        }

        public String getDefault_setting() {
            return default_setting;
        }

        public void setDefault_setting(String default_setting) {
            this.default_setting = default_setting;
        }

        public String getCamera_clock() {
            return camera_clock;
        }

        public void setCamera_clock(String camera_clock) {
            this.camera_clock = camera_clock;
        }

        public String getPhoto_size() {
            return photo_size;
        }

        public void setPhoto_size(String photo_size) {
            this.photo_size = photo_size;
        }

        public String getFrequency() {
            return frequency;
        }

        public void setFrequency(String frequency) {
            this.frequency = frequency;
        }

        public String getVideo_wb() {
            return video_wb;
        }

        public void setVideo_wb(String video_wb) {
            this.video_wb = video_wb;
        }

        public String getVideo_color() {
            return video_color;
        }

        public void setVideo_color(String video_color) {
            this.video_color = video_color;
        }

        public String getVideo_ev() {
            return video_ev;
        }

        public void setVideo_ev(String video_ev) {
            this.video_ev = video_ev;
        }

        public String getVideo_meter() {
            return video_meter;
        }

        public void setVideo_meter(String video_meter) {
            this.video_meter = video_meter;
        }

        public String getVideo_contrast() {
            return video_contrast;
        }

        public void setVideo_contrast(String video_contrast) {
            this.video_contrast = video_contrast;
        }

        public String getPhoto_wb() {
            return photo_wb;
        }

        public void setPhoto_wb(String photo_wb) {
            this.photo_wb = photo_wb;
        }

        public String getPhoto_color() {
            return photo_color;
        }

        public void setPhoto_color(String photo_color) {
            this.photo_color = photo_color;
        }

        public String getPhoto_ev() {
            return photo_ev;
        }

        public void setPhoto_ev(String photo_ev) {
            this.photo_ev = photo_ev;
        }

        public String getPhoto_meter() {
            return photo_meter;
        }

        public void setPhoto_meter(String photo_meter) {
            this.photo_meter = photo_meter;
        }

        public String getPhoto_contrast() {
            return photo_contrast;
        }

        public void setPhoto_contrast(String photo_contrast) {
            this.photo_contrast = photo_contrast;
        }

        public String getPhoto_iso() {
            return photo_iso;
        }

        public void setPhoto_iso(String photo_iso) {
            this.photo_iso = photo_iso;
        }

        public String getVideo_iso() {
            return video_iso;
        }

        public void setVideo_iso(String video_iso) {
            this.video_iso = video_iso;
        }

        public String getPhoto_shutter() {
            return photo_shutter;
        }

        public void setPhoto_shutter(String photo_shutter) {
            this.photo_shutter = photo_shutter;
        }

        public String getVideo_shutter() {
            return video_shutter;
        }

        public void setVideo_shutter(String video_shutter) {
            this.video_shutter = video_shutter;
        }

        public String getSystem_type() {
            return system_type;
        }

        public void setSystem_type(String system_type) {
            this.system_type = system_type;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getPhoto_vr_mode() {
            return photo_vr_mode;
        }

        public void setPhoto_vr_mode(String photo_vr_mode) {
            this.photo_vr_mode = photo_vr_mode;
        }

        public void setPhoto_time_lapse_mode(String photo_time_lapse_mode) {
            this.photo_time_lapse_mode = photo_time_lapse_mode;
        }

        public String getPhoto_time_lapse_mode() {
            return photo_vr_mode;
        }

        public String getPhoto_raw() {
            return photo_raw;
        }

        public void setPhoto_raw(String photo_raw) {
            this.photo_raw = photo_raw;
        }

        public String getVideo_zoom() {
            return video_zoom;
        }

        public void setVideo_zoom(String video_zoom) {
            this.video_zoom = video_zoom;
        }

        public String getPhoto_zoom() {
            return photo_zoom;
        }

        public void setPhoto_zoom(String photo_zoom) {
            this.photo_zoom = photo_zoom;
        }

        public String getVideo_image_mode() {
            return video_image_mode;
        }

        public void setVideo_image_mode(String video_image_mode) {
            this.video_image_mode = video_image_mode;
        }

        public String getPhoto_image_mode() {
            return photo_image_mode;
        }

        public void setPhoto_image_mode(String photo_image_mode) {
            this.photo_image_mode = photo_image_mode;
        }

        public String getVideo_saturation() {
            return video_saturation;
        }

        public void setVideo_saturation(String video_saturation) {
            this.video_saturation = video_saturation;
        }

        public String getPhoto_saturation() {
            return photo_saturation;
        }

        public void setPhoto_saturation(String photo_saturation) {
            this.photo_saturation = photo_saturation;
        }

        public String getVideo_sharpness() {
            return video_sharpness;
        }

        public void setVideo_sharpness(String video_sharpness) {
            this.video_sharpness = video_sharpness;
        }

        public String getPhoto_sharpness() {
            return photo_sharpness;
        }

        public void setPhoto_sharpness(String photo_sharpness) {
            this.photo_sharpness = photo_sharpness;
        }

        public String getVideo_10bit_mode() {
            return video_10bit_mode;
        }

        public void setVideo_10bit_mode(String video_10bit_mode) {
            this.video_10bit_mode = video_10bit_mode;
        }

        public String getFen_mode() {
            return fen_mode;
        }

        public void setFen_mode(String fen_mode) {
            this.fen_mode = fen_mode;
        }

        public String getFree_size() {
            return free_size;
        }

        public void setFree_size(String free_size) {
            this.free_size = free_size;
        }

        public String getTotal_size() {
            return total_size;
        }

        public void setTotal_size(String total_size) {
            this.total_size = total_size;
        }

        @Override
        public String toString() {
            return "ParamBean{" +
                    "video_quality='" + video_quality + '\'' +
                    ", video_resolution='" + video_resolution + '\'' +
                    ", default_setting='" + default_setting + '\'' +
                    ", camera_clock='" + camera_clock + '\'' +
                    ", photo_size='" + photo_size + '\'' +
                    ", frequency='" + frequency + '\'' +
                    ", video_wb='" + video_wb + '\'' +
                    ", video_color='" + video_color + '\'' +
                    ", video_ev='" + video_ev + '\'' +
                    ", video_meter='" + video_meter + '\'' +
                    ", video_contrast='" + video_contrast + '\'' +
                    ", photo_wb='" + photo_wb + '\'' +
                    ", photo_color='" + photo_color + '\'' +
                    ", photo_ev='" + photo_ev + '\'' +
                    ", photo_meter='" + photo_meter + '\'' +
                    ", photo_contrast='" + photo_contrast + '\'' +
                    ", photo_iso='" + photo_iso + '\'' +
                    ", photo_shutter='" + photo_shutter + '\'' +
                    ", system_type='" + system_type + '\'' +
                    ", version='" + version + '\'' +
                    ", photo_vr_mode='" + photo_vr_mode + '\'' +
                    ", photo_raw='" + photo_raw + '\'' +
                    ", video_zoom='" + video_zoom + '\'' +
                    ", photo_zoom='" + photo_zoom + '\'' +
                    ", video_image_mode='" + video_image_mode + '\'' +
                    ", photo_image_mode='" + photo_image_mode + '\'' +
                    ", video_saturation='" + video_saturation + '\'' +
                    ", photo_saturation='" + photo_saturation + '\'' +
                    ", video_sharpness='" + video_sharpness + '\'' +
                    ", photo_sharpness='" + photo_sharpness + '\'' +
                    ", free_size='" + free_size + '\'' +
                    ", total_size='" + total_size + '\'' +
                    ", video_3a_lock='" + video_3a_lock + '\'' +
                    ", photo_3a_lock='" + photo_3a_lock + '\'' +
                    ", video_iso='" + video_iso + '\'' +
                    ", video_shutter='" + video_shutter + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ABSettingBean{" +
                "rval=" + rval +
                ", msg_id=" + msg_id +
                ", param=" + param +
                '}';
    }
}
