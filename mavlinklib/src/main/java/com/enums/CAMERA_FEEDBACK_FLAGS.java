            /** 
            */
            package com.enums;
            
            public class CAMERA_FEEDBACK_FLAGS {
            	public static final int VIDEO = 1; /* Shooting video, not stills | */
            	public static final int BADEXPOSURE = 2; /* Unable to achieve requested exposure (e.g. shutter speed too low) | */
            	public static final int CLOSEDLOOP = 3; /* Closed loop feedback from camera, we know for sure it has successfully taken a picture | */
            	public static final int OPENLOOP = 4; /* Open loop camera, an image trigger has been requested but we can't know for sure it has successfully taken a picture | */
            	public static final int CAMERA_FEEDBACK_FLAGS_ENUM_END = 5; /*  | */
            
            }
            