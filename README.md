## 1.Import and use SDK
### 1.1	import "module" project "fitpolosupport"
### 1.2	settings"settings.gradle"，reference "fitpolosupport" project：

	include ':app',':fitpolosupport'

### 1.3	Edit the 'build.gradle' in the main project：

	dependencies {
	    implementation fileTree(dir: 'libs', include: ['*.jar'])
	    implementation project(path: ':fitpolosupport')
	}

### 1.4	import the SDK when initiating：

	public class BaseApplication extends Application {
	    @Override
	    public void onCreate() {
	        super.onCreate();
	        // init
	        MokoSupport.getInstance().init(getApplicationContext());
	    }
	}


## 2.Function Introduction

- There are many method in the SDK, including san device, pair, send command, get the Bluetooth status of phone, get the connection status, disconnect the band.
- Because the scan and receive data is asynchronous, to APP get the data in background mode, so we suggest that you call all the method in the `Service`；
- the method can be called in the`MokoSupport.getInstance()`；

### 2.1	startScanDevice

	@Description scan device
	public void startScanDevice(final MokoScanDeviceCallback callback) {}

callback function`MokoScanDeviceCallback`：

	@Description scan device callback
	public interface ScanDeviceCallback {
	    @Description start scan
	    void onStartScan();
	    @Description scan device
	    void onScanDevice(BleDevice device);
	    @Description stop scan
	    void onStopScan();
	}

- Do prepratiton before scan in`onStartScan()`

- receive the scanned device in`onScanDevice(BleDevice device)`,including MAC address, name, int rssi and scanrecord.

		public class BleDevice implements Serializable, Comparable<BleDevice> {
		    public String address;
		    public String name;
		    public int rssi;
		    public String verifyCode;
		    public byte[] scanRecord;
			...
		}
	For example：
		BleDevice{address='DA:22:C3:C7:7D', name='FitpolpHR', rssi=-38, verifyCode='0C8D02', scanRecord=\[2,1,6,7...\]}

- Do the handle work after scan in`onStopScan()`.

### 2.2	createBluetoothGatt

	@Description pair device
	public void connDevice(Context context, String address, MokoConnStateCallback mokoConnStateCallback) {}

incoming parameters：

1. context.

2. Device MAC Address.

3. callback function`MokoConnStateCallback`；


	@Description Front display connection callback
	public interface MokoConnStateCallback {
	    @Description connection succeed
	    void onConnectSuccess();
	    @Description  disconnect
	    void onDisConnected();
	    @Description  Reconnection timeout
	    void onConnTimeout(int reConnCount);
	}

- `onConnectSuccess()`connect successfully；

- `onDisConnected()`connect failed；

- `onConnTimeout(int reConnCount)` connect failed，reConnCount；connection times

### 2.3	setOpenReConnect

	@Description set re-connect
    public void setOpenReConnect(boolean openReConnect){}

- `openReConnect`is true，then open the bluetooth,When the connection fails or disconnects, the system will perform connection thread. If close the bluetooth, connection will be done every 30 seconds.If re-connect fail, continue to re-connect；
- `openReConnect`is false，then close the bluetooth and reconnect.

### 2.4	sendOrder

	@Description //send order
	public void sendOrder(OrderTask... orderTasks){}

- Can send a single order；
- can send several orders, orders are handled in line, first in first out. 

Abstract class order, including order enumeration, command response callback, command response results

	public abstract class OrderTask {
		public OrderType orderType;
		public OrderEnum order;
	    public MokoOrderTaskCallback callback;
	    public OrderTaskResponse response;
	}

1. OrderType

		public enum OrderType implements Serializable {
			NOTIFY("NOTIFY", "0000ffc2-0000-1000-8000-00805f9b34fb"),
			WRITE("WRITE", "0000ffc1-0000-1000-8000-00805f9b34fb"),
			;

			private String uuid;
			private String name;

			OrderType(String name, String uuid) {
				this.name = name;
				this.uuid = uuid;
			}

			public String getUuid() {
				return uuid;
			}

			public String getName() {
				return name;
			}
		}

	- Command types: each command belong one type, the current H701 only have notification and write two types, notification type only could be sent after connecte with the device, open the device notifications, the rest of the command can send to the device to receive reply by writing type

2. OrderEnum

		public enum OrderEnum implements Serializable {
			getInnerVersion("Get the internal version", 0x09),
			setSystemTime("set the bracelet time" , 0x11),
			setUserInfo("set user profile" , 0x12),
			setBandAlarm("set alarm data", 0x26),
			...
			private String orderName;
			private int orderHeader;

			OrderEnum(String orderName, int orderHeader) {
				this.orderName = orderName;
				this.orderHeader = orderHeader;
			}

			public int getOrderHeader() {
				return orderHeader;
			}

			public String getOrderName() {
				return orderName;
			}
		｝
	- orderName: order name； 
	- orderHeader: distinguish order header；
	- 	- Different orders for different enumerated types, when performing multiple orders, according to the type to judge which command response

3. MokoOrderTaskCallback

		public interface MokoOrderTaskCallback {
			// response success
		    void onOrderResult(OrderTaskResponse response);
			// response timeout
		    void onOrderTimeout(OrderTaskResponse response);
			// complete the command
		    void onOrderFinish();
		}

	- onOrderResult(OrderTaskResponse response) response successful，response include OrderEnum，can judge it is which order result by OrderEnum；
	- onOrderTimeout(OrderTaskResponse response)  response timeout，response include OrderEnum，can judge it is which order timeout by OrderEnum；
	- onOrderFinish() order finish, when no order in the line, back this method；
	

4. OrderTaskResponse

		public class OrderTaskResponse implements Serializable {
			public OrderEnum order;
			public int responseType;
			public byte[] responseValue;
		}

	- responseType:`RESPONSE_TYPE_NOTIFY`和`RESPONSE_TYPE_WRITE_NO_RESPONSE` two types，distinguish order type；
	- responseValue: response the Returned data

OrderTask的子类：

	1.Get innerversion Task
		InnerVersionTask
		After return the results, can get bracelet information. The method is as follows:
		MokoSupport.showHeartRate;//if support Sync heart rate.
		MokoSupport.supportNewData;//if support sync the latest datas.
		MokoSupport.supportNotifyAndRead;//if support read datas and SMS notification.
		MokoSupport.firmwareEnum;//get firmware version.
		MokoSupport.canUpgrade;//if can upgrade.
	2.set system time task
		SystemTimeTask
	3.set user info task
		UserInfoTask
		incoming parameters need introduce UserInfo
	4.set band alarm task
		AllAlarmTask
		incoming parameters need introduce band alarm info List<BandAlarm>
		public class BandAlarm {
		    public String time;// time，format：HH:mm
		    // state
		    // bit[7]：0：close；1：poen；
		    // bit[6]：1：sunday；
		    // bit[5]：1：saturday；
		    // bit[4]：1：friday；
		    // bit[3]：1：thursday；
		    // bit[2]：1：wednesday；
		    // bit[1]：1：tuesday；
		    // bit[0]：1：monday；
		    // ex：Every sunday open：11000000；Every Monday to Friday open 10011111；
		    public String state;
		    public int type;// type，0：medicine；1：water；3：normal；4：sleep；5：exercise；6：sport；
		｝
	5.set unit type task
		UnitTypeTask
		incoming parameters need introduce unit type
		unitType// 0：Chinese；1：English，default chinese
	6.set time format task
		TimeFormatTask
		incoming parameters need introduce time show format
		timeFormat;// 0：24；1：12，default 24-hour
	7.set automatic lighten the screen
		AutoLightenTask
		incoming parameters need introduce if automatic lighten the screen
		autoLighten;// 0：open；1：close，default open
	8.set sitlongtime alert task
		SitLongTimeAlertTask
		incoming parameters need introduce sit long time alert info: SitAlert
		public class SitAlert {
		    public int alertSwitch; // sit long time alart switch，1：open；0：close
		    public String startTime;// start time, format：HH:mm;
		    public String endTime;// end time, format：HH:mm;
		｝
	9.set last show task
		LastScreenTask
		incoming parameters need introduce LastShowTask
		lastScreen;// 1：open；0：close
	10.set Heart Rate IntervalTask
		HeartRateIntervalTask
		incoming parameters need introduce HeartRateIntervalTask
		heartRateInterval;// 0：close；1：ten min；2：twenty min；3：thirty min
	11.set function display task
		FunctionDisplayTask
		incoming parameters need introduce FunctionDisplayTask
		CustomScreen;
	    // duration：Whether to show the movement time；
	    // calorie：Whether to show burn calories；
	    // distance：Whether to show movement distance；
	    // heartrate：Whether to show heart rate；
	    // step：Whether to show steps；
	12.get firmware version task
		FirmwareVersionTask
		After return results, can check the firm version
		MokoSupport.versionCodeShow
		for example：
		MokoSupport.versionCodeShow = "2.1.32"
	13.get battery and daily steps count task
		BatteryDailyStepsCountTask
		After return results, can check the battery power
		MokoSupport.getInstance().getBatteryQuantity()
	14.get sleep and heart rate task
		SleepHeartCountTask
	15.get daily steps task
		AllStepsTask
		After return results, can check the all steps data in the bracelet
		MokoSupport.getInstance().getDailySteps();
		public class DailyStep {
		    public String date;// date，yyyy-MM-dd
		    public String count;// steps
		    public String duration;// sports time
		    public String distance;// sports distance
		    public String calories;// burnt calories
			...
		}
		examples：
		DailyStep{date='2017-06-05', count='1340', duration='5', distance='0.9', calories='78'}
	16.get sleep date
		AllSleepIndexTask
		After return results, can check the all sleep data in the bracelet
		MokoSupport.getInstance().getDailySleeps()
		public class DailySleep {
		    public String date;// date，yyyy-MM-dd
		    public String startTime;// start time，yyyy-MM-dd HH:mm
		    public String endTime;// end time，yyyy-MM-dd HH:mm
		    public String deepDuration;// deep sleep time，unit: min
		    public String lightDuration;// light sleep time，unit: min
		    public String awakeDuration;// wake up time，unit: min
		    public List<String> records;// sleep record
			...
		}
		for example：
		DailySleep{date='2017-06-05', startTime='2017-06-04 23:00', endTime='2017-06-05 07:00', deepDuration='360', lightDuration='60', awakeDuration='60' records=['01','01','10','10','00',...]}
	17.get heart rate data
		AllHeartRateTask
		After return results, can check the all heart rate data in the bracelet
		MokoSupport.getInstance().getHeartRates();
		public class HeartRate implements Comparable<HeartRate> {
		    public String time;
		    public String value;
			...
		}
		for example：
		HeartRate{time='2017-06-05 12:00', value='78'}
	18.Get hardware parameters
		FirmwareParamTask
		After returning the result, you can check the firmware parameters
		MokoSupport.getInstance().getLastChargeTime();//Last charging time
		MokoSupport.getInstance().getProductBatch();//Production batch number
	19.Get unsynced steps data task
		LastestStepsTask
		incoming parameters need introduce timestamp
		lastSyncTime;// yyyy-MM-dd HH:mm
		After return results, can check step data after special timestamp
		MokoSupport.getInstance().getDailySteps()
	20.Get unsynced sleep data task
		LastestSleepIndexTask
		incoming parameters need introduce timestamp
		lastSyncTime;// yyyy-MM-dd HH:mm
		After return results, can check sleep data after special timestamp
		MokoSupport.getInstance().getDailySleeps()
	21.Get unsynced heart rate data task
		LastestHeartRateTask
		incoming parameters need introduce timestamp
		lastSyncTime;// yyyy-MM-dd HH:mm
		After return results, can check heart rate data after special timestamp
		MokoSupport.getInstance().getHeartRates()
	22.set shake band task
		ShakeBandTask
		default shake twice, shakes 1 second stop 1 second
		no response to deal with
	23.set phonecall coming shake task
		PhoneComingShakeTask
		incoming parameters need introduce show text, whether it is cellphone no. or contact person name
		String showText;// show contents(tel No. or contacts)
    	boolean isPhoneNumber;// Whether it is Tel No.
		no response to deal with
	24.set SMS coming shake task
		SmsComingShakeTask
		incoming parameters need introduce show text, whether it is cellphone no. or contact person name
		String showText;// show contents(tel No. or contacts)
    	boolean isPhoneNumber;// Whether it is Tel No.
		no response to deal with

	The below functions are only available for firmware version 32 or above

	25.Read alarm datas
		ReadAlarmsTask
		MokoSupport.getInstance().getAlarms()
	26.Read sedentary reminder datas
		ReadSitAlertTask
		MokoSupport.getInstance().getSitAlert()
	27.Read bracelet setting datas
		ReadSettingTask
		MokoSupport.getInstance().getUnitTypeBritish();//Unit type
		MokoSupport.getInstance().getTimeFormat();//Time fo
		MokoSupport.getInstance().getCustomScreen();//Functions display
		MokoSupport.getInstance().getLastScreen();//Whether turn on the last screen
		MokoSupport.getInstance().getHeartRateInterval();//Heart rateinterval
		MokoSupport.getInstance().getAutoLighten();//Whether turn on switch wrist to brght screen
	28.wechat notification
		NotifyWechatTask 
		Input parameters need show text
		String showText;// show text 
		no response 
	29.qq notify
		NotifyQQTask 
		Input parameters need show text
		String showText;// show text
		no response
	30.WhatsApp notify
		NotifyWhatsAppTask
		Input parameters need show text
		String showText;// show text
		no response
	31.Facebook notify
		NotifyFacebookTask
		Input parameters need show text
		String showText;// show text
		no response
	32.Twitter notify
		NotifyTwitterTask
		Input parameters need show text
		String showText;//show text
		no response
	33.Skype notify
		NotifySkypeTa sk
		Input parameters need show text
		String showText;//show text
		no response
	34.Snapchat notify
		NotifySnapchatTask
		Input parameters need show text
		String showText;//show text
		no response
	35.Line notify
		NotifyLineTask
		Input parameters need show text
		String showText;//show text
		no response

### 2.5	sendDirectOrder

Send commands directly, when command no need response, this method could be used, only support sending a single command

	public void sendDirectOrder(OrderTask orderTask){}

### 2.6	isBluetoothOpen

judge whether the bluetooth is open or close

	public boolean isBluetoothOpen(){}

### 2.7	isConnDevice

judge whether the braclet is connected or not

	public boolean isConnDevice(Context context, String address){}

bracelet MAC address

### 2.8	disConnectBle

disconnect wiht bracelet

	public void disConnectBle(){}

## 3.Save Log to SD Card

- SDK integrates the function of save the Log to SD card, is referenced [https://github.com/elvishew/xLog](https://github.com/elvishew/xLog "XLog")
- initialize method could be achieved in `MokoSupport.getInstance().init(getApplicationContext())`
- Can be modified the document name and folder name saved in the SD card

		public class LogModule {
			private static final String TAG = "fitpoloDemo";// document name
		    private static final String LOG_FOLDER = "fitpoloDemo";// folder name
			...
		}

- storage strategy: only to save today data and the previous day data, the previous day data`.Bak` as suffix 
- calling mode：
	- LogModule.v("log info");
	- LogModule.d("log info");
	- LogModule.i("log info");
	- LogModule.w("log info");
	- LogModule.e("log info");

## 4.Upgrade

- The upgrade function is encapsulated in com.fitpolo.support.handler.UpgradeHandler, using it as following way：


		UpgradeHandler upgradeHandler = new UpgradeHandler(this);
		upgradeHandler.setFilePath(firmwarePath, deviceMacAddress,
		new UpgradeHandler.IUpgradeCallback() {
	        @Override
	        public void onUpgradeError(int errorCode) {
	            switch (errorCode) {
	                case UpgradeHandler.EXCEPTION_FILEPATH_IS_NULL:
	                    break;
	                case UpgradeHandler.EXCEPTION_DEVICE_MAC_ADDRESS_IS_NULL:
	                    break;
	                case UpgradeHandler.EXCEPTION_UPGRADE_FAILURE:
	                    break;
	            }
	        }

	        @Override
	        public void onProgress(int progress) {

	        }

	        @Override
	        public void onUpgradeDone() {

	        }
	    });

- income three parameters while callback：
	- firmwarePath：Upgrade the firmware path；
	- deviceMacAddress：Device Mac address(could get it from the scanned device )；
	- IUpgradeCallback：Upgrade the callback interface to implement the methods in the interface , obtain the upgrade failure, upgrade progress, and upgrade successful callback；

- noted：
	-	When upgrade, can not send other data to the bracelet;
	-	When the upgrade starts, the bracelet will be disconnected first and automatically connected after 4 seconds to make the bracelet enter the high-speed mode for transmitting data.；
	-	After the upgrade fails or succeeds, the bracelet will be disconnected again and needs to be reconnected.；



