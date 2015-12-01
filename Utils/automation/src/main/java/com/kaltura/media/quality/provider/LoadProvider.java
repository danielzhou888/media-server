package com.kaltura.media.quality.provider;


import java.util.Date;

import sun.management.ManagementFactoryHelper;

import com.kaltura.media.quality.event.Event;
import com.kaltura.media.quality.event.EventsManager;
import com.kaltura.media.quality.event.listener.IListener;
import com.kaltura.media.quality.event.listener.ILoadListener;
import com.kaltura.media.quality.utils.ThreadManager;
import com.sun.management.OperatingSystemMXBean;


public class LoadProvider extends Provider {

	private static final long serialVersionUID = -2514958859459044247L;
	private static LoadProvider instance = null;

	class LoadStatusEvent extends Event<ILoadListener>{

		private static final long serialVersionUID = -4372120611277234264L;
		private Date time;
		private double loadAverage;
		private double cpu;
		private double physicalMemory;

		public LoadStatusEvent(Date time, double loadAverage, double cpu, double physicalMemory) {
			super(ILoadListener.class);

			this.loadAverage = loadAverage;
			this.cpu = cpu;
			this.physicalMemory = physicalMemory;
			this.time = time;
		}

		@Override
		protected void callListener(ILoadListener listener) {
			listener.onLoadResult(time, loadAverage, cpu, physicalMemory);
		}

		@Override
		protected String getTitle() {
			return "" + time;
		}
	}
	
	@Override
	public void run() {
		OperatingSystemMXBean operatingSystemBean = (OperatingSystemMXBean) ManagementFactoryHelper.getOperatingSystemMXBean();

		Date time;
		double loadAverage;
		double cpu;
		double physicalMemory;
		
		while(ThreadManager.shouldContinue()){
			time = new Date();
			loadAverage = operatingSystemBean.getSystemLoadAverage();
			cpu = operatingSystemBean.getSystemCpuLoad();
			physicalMemory = operatingSystemBean.getTotalPhysicalMemorySize();
			
			EventsManager.get().raiseEvent(new LoadStatusEvent(time, loadAverage, cpu, physicalMemory));
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	public static synchronized void init() {
		if(instance == null){
			instance = new LoadProvider();
			ThreadManager.start(instance);
		}
	}

	@Override
	public boolean isDeffered() {
		return false;
	}

	@Override
	public int compareTo(IListener o) {
		if(o == this){
			return 0;
		}
		
		return 1;
	}

	@Override
	public void register() {
	}

	@Override
	public String getTitle() {
		return null;
	}

}
