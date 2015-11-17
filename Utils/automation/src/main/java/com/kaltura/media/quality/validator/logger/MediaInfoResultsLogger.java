package com.kaltura.media.quality.validator.logger;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.kaltura.media.quality.configurations.LoggerConfig;
import com.kaltura.media.quality.event.EventsManager;
import com.kaltura.media.quality.event.listener.IListener;
import com.kaltura.media.quality.event.listener.ISegmentInfoListener;
import com.kaltura.media.quality.model.Segment;
import com.kaltura.media.quality.validator.info.FFPROBE;
import com.kaltura.media.quality.validator.info.MediaInfo;
import com.kaltura.media.quality.validator.info.MediaInfoBase.Info;

public class MediaInfoResultsLogger extends ResultsLogger implements ISegmentInfoListener {
    private static final Logger log = Logger.getLogger(MediaInfoResultsLogger.class);

	public MediaInfoResultsLogger(String uniqueId, LoggerConfig loggerConfig) throws IOException {
		super(uniqueId, loggerConfig.getName());
		EventsManager.get().addListener(ISegmentInfoListener.class, this);
	}

	class MediaInfoResult implements IResult{
		private Segment segment;
		private FFPROBE.FFPROBEInfo ffprobeInfo;
		private MediaInfo.MediaInfoInfo mediaInfoInfo;
		
		public MediaInfoResult(Segment segment) {
			this.segment = segment;
		}

		@Override
		public Object[] getValues(){
			try{
				segment.getDuration();
				ffprobeInfo.getDuration();
			}
			catch(NullPointerException n){
				return new Object[]{};
			}
			double expectedDurationDiff = 10 - segment.getDuration();
			double ffprobeContainerDurationDiff = segment.getDuration() - ffprobeInfo.getDuration();
			double ffprobeVideoDurationDiff = segment.getDuration() - ffprobeInfo.getVideo().getDuration();
			double ffprobeAudioDurationDiff = segment.getDuration() - ffprobeInfo.getAudio().getDuration();
			int ffprobeWidthDiff = segment.getRendition().getWidth() - ffprobeInfo.getVideo().getWidth();
			int ffprobeHeightDiff = segment.getRendition().getHeight() - ffprobeInfo.getVideo().getHeight();
			int ffprobeBitrateDiff = segment.getRendition().getBandwidth() - ffprobeInfo.getBitrate();

			double mediaInfoContainerDurationDiff = segment.getDuration() - mediaInfoInfo.getDuration();
			double mediaInfoVideoDurationDiff = segment.getDuration() - mediaInfoInfo.getVideo().getDuration();
			double mediaInfoAudioDurationDiff = segment.getDuration() - mediaInfoInfo.getAudio().getDuration();
			int mediaInfoWidthDiff = segment.getRendition().getWidth() - mediaInfoInfo.getVideo().getWidth();
			int mediaInfoHeightDiff = segment.getRendition().getHeight() - mediaInfoInfo.getVideo().getHeight();
			int mediaInfoBitrateDiff = segment.getRendition().getBandwidth() - mediaInfoInfo.getBitrate();
			int mediaInfoVideoMaxBitrateDiff = segment.getRendition().getBandwidth() - mediaInfoInfo.getVideo().getMaximumBitrate();
			
			return new Object[]{
				segment.getRendition().getDomainHash(),
				segment.getRendition().getBandwidth(),
				segment.getRendition().getResolution(),
				
				segment.getNumber(),
				segment.getDuration(),
				expectedDurationDiff,
				
				ffprobeInfo.getFormat().getProbeScore(),
				ffprobeInfo.getFormat().getStartTime(),
				ffprobeInfo.getVideo().getStartTime(),
				ffprobeInfo.getAudio().getStartTime(),
				
				ffprobeContainerDurationDiff,
				ffprobeVideoDurationDiff,
				ffprobeAudioDurationDiff,
				ffprobeWidthDiff,
				ffprobeHeightDiff,
				ffprobeBitrateDiff,
				
				mediaInfoInfo.getAudio().getDelayRelativeToVideo(),

				mediaInfoContainerDurationDiff,
				mediaInfoVideoDurationDiff,
				mediaInfoAudioDurationDiff,
				mediaInfoWidthDiff,
				mediaInfoHeightDiff,
				mediaInfoBitrateDiff,
				mediaInfoVideoMaxBitrateDiff
				
			};
		}

		@Override
		public String[] getHeaders() {
			return new String[]{
				"Domain Hash",
				"Rendition Bitrate",
				"Rendition Resolution",
				
				"Segment Number",
				"Duration",
				"Expected Duration Diff",
				
				"FFprobe Score",
				"FFprobe Container Start Time",
				"FFprobe Video Start Time",
				"FFprobe Audio Start Time",
				
				"FFprobe Container Duration Diff",
				"FFprobe Video Duration Diff",
				"FFprobe Audio Duration Diff",
				"FFprobe Width Diff",
				"FFprobe Height Diff",
				"FFprobe Bitrate Diff",
				
				"Media-info Audio Delay",
				
				"Media-info Container Duration Diff",
				"Media-info Video Duration Diff",
				"Media-info Audio Duration Diff",
				"Media-info Width Diff",
				"Media-info Height Diff",
				"Media-info Bitrate Diff",
				"Media-info Video Max Bitrate Diff"
			};
		}

		public void setInfo(FFPROBE.FFPROBEInfo info) {
			ffprobeInfo = info;
		}
		
		public void setInfo(MediaInfo.MediaInfoInfo info) {
			mediaInfoInfo = info;
		}
	}
	
	protected void write(MediaInfoResult result) {
		super.write(result);
	}

	@Override
	public int compareTo(IListener o) {
		if(o == this){
			return 0;
		}
		
		return 1;
	}

	@Override
	public void onSegmentInfo(Segment segment, List<Info> infos) {
		if(!segment.getEntryId().equals(uniqueId)){
			return;
		}

		MediaInfoResult result = new MediaInfoResult(segment);
		
		for(Info info : infos){
			if(info instanceof FFPROBE.FFPROBEInfo){
				result.setInfo((FFPROBE.FFPROBEInfo) info);
			}
			if(info instanceof MediaInfo.MediaInfoInfo){
				result.setInfo((MediaInfo.MediaInfoInfo) info);
			}
		}

		try{
			write(result);
		} catch (Exception e) {
			log.error("Error on segment file [" + segment.getFile().getAbsolutePath() + "]:" + e.getMessage(), e);
			return;
		}
	}
}
