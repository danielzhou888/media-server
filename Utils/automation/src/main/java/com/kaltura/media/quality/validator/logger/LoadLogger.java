package com.kaltura.media.quality.validator.logger;

import java.io.IOException;



import java.util.Date;

import com.kaltura.media.quality.configurations.LoggerConfig;
import com.kaltura.media.quality.event.EventsManager;
import com.kaltura.media.quality.event.listener.IListener;
import com.kaltura.media.quality.event.listener.ILoadListener;
import com.kaltura.media.quality.provider.LoadProvider;

public class LoadLogger extends ResultsLogger implements ILoadListener {
	
	public LoadLogger(String uniqueId, LoggerConfig loggerConfig) throws IOException {
		super(uniqueId, loggerConfig.getName());
		
		EventsManager.get().addListener(ILoadListener.class, this);
		LoadProvider.init();
	}

	class LoadResult implements IResult{
		private Date time;
		private double loadAverage;
		private double cpu;
		private double physicalMemory;
		
		public LoadResult(Date time, double loadAverage, double cpu, double physicalMemory) {
			this.time = time;
			this.loadAverage = loadAverage;
			this.cpu = cpu;
			this.physicalMemory = physicalMemory;
		}


		@Override
		public Object[] getValues(){
			return new Object[]{
				time,
				loadAverage,
				cpu,
				physicalMemory
			};
		}


		@Override
		public String[] getHeaders() {
			return new String[]{
				"Time",
				"Load Average",
				"CPU",
				"Physical Memory"
			};
		}
	}
	
	protected void write(LoadResult result){
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
	public void onLoadResult(Date time, double loadAverage, double cpu, double physicalMemory) {
		write(new LoadResult(time, loadAverage, cpu, physicalMemory));
	}
}
