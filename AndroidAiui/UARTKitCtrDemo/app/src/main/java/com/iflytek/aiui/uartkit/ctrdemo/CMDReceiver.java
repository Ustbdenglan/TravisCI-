package com.iflytek.aiui.uartkit.ctrdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.IllegalFormatFlagsException;

import org.json.JSONException;
import org.json.JSONObject;

import com.iflytek.aiui.uartkit.UARTAgent;
import com.iflytek.aiui.uartkit.entity.WIFIConfPacket.EncryptMethod;
import com.iflytek.aiui.uartkit.entity.WIFIConfPacket.WIFIStatus;
import com.iflytek.aiui.uartkit.util.PacketBuilder;

public class CMDReceiver extends BroadcastReceiver {
	private static final String TAG = "VoiceCtrlReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		UARTAgent uartAgent = UARTAgent.getUARTAgent();
		Log.d(TAG, "receiver start");
		if(intent.hasExtra("voice_control")){
			String play_mode = intent.getStringExtra("play_mode");
			if ("enable".equals(play_mode)) {
				uartAgent.sendMessage(PacketBuilder.obtainVoiceCtrPacket(true));
			} else if ("disable".equals(play_mode)) {
				uartAgent.sendMessage(PacketBuilder.obtainVoiceCtrPacket(false));
			}
		}
		
		if(intent.hasExtra("wifi_conf")){
			String ssid = intent.getStringExtra("ssid");
			String passwd = intent.getStringExtra("passwd");
			if(ssid == null || passwd == null){
				Log.d(TAG, "ssid or passwd is empty!");
				return;
			}
			
			uartAgent.sendMessage(PacketBuilder.obtainWIFIConfPacket(WIFIStatus.CONNECTED, EncryptMethod.WPA, ssid, passwd));
		}
		
		if(intent.hasExtra("wifi_status")){
			uartAgent.sendMessage(PacketBuilder.obtainWIFIStatusReqPacket());
		}
		
		if(intent.hasExtra("aiui_conf")){
			String appid = intent.getStringExtra("appid");
			String key = intent.getStringExtra("key");
			String scene = intent.getStringExtra("scene");
			boolean launchDemo = intent.getBooleanExtra("launchDemo", false);
			if(appid == null || key == null || scene == null){
				Log.d(TAG, "appid or key or scene is null!!");
				return;
			}
			uartAgent.sendMessage(PacketBuilder.obtainAIUIConfPacket(appid, key, scene, launchDemo));
		}
		
		if(intent.hasExtra("audio_record")){
			uartAgent.sendMessage(PacketBuilder.obtainAIUIAudioRecordCmdPacket());
		}
		
		if(intent.hasExtra("aiui_message")){
			int msgType = intent.getIntExtra("msgType", 1);
			int arg1 = intent.getIntExtra("arg1", 0);
			int arg2 = intent.getIntExtra("arg2", 0);
			String params = intent.getStringExtra("params");
			
			uartAgent.sendMessage(PacketBuilder.obtainAIUICtrPacket(msgType, arg1, arg2, params));
		}
		
		if(intent.hasExtra("aiui_extra")){
			uartAgent.sendMessage(PacketBuilder.obtainCustomPacket(new byte[]{1, 1, 0}));
		}
		
		if(intent.hasExtra("tts_start")){
			String ttsText = intent.getStringExtra("text");
			uartAgent.sendMessage(PacketBuilder.obtainTTSStartPacket(ttsText));
		}
		
		if(intent.hasExtra("tts_stop")){
			uartAgent.sendMessage(PacketBuilder.obtainTTSStopPacket());
		}
		
		if(intent.hasExtra("sync")){

			 //同步操作。
			int CMD_SYNC = 13;
			
			//同步状态数据。
			int SYNC_DATA_STATUS = 0;
			
			//同步个性化数据。
			int SYNC_DATA_INDIVIDUAL = 1;
			
			//第三方账号关联数据。
			int SYNC_DATA_ACCOUNT = 2;
			
			if(intent.hasExtra("status")){
				try {
					JSONObject syncParamsJson = new JSONObject();
					syncParamsJson.put("withSign", "0");
					
					// 状态同步
					JSONObject syncStatusJson = new JSONObject();
					syncStatusJson.put("key1", "val1");
					syncStatusJson.put("key2", "val2");
					
					byte[] syncData = syncStatusJson.toString().getBytes("utf-8");
					
					uartAgent.sendMessage(PacketBuilder.obtainAIUICtrPacket(
							CMD_SYNC, SYNC_DATA_STATUS, 0, syncParamsJson.toString(), syncData));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if(intent.hasExtra("individual")){
				try {
					JSONObject syncParamsJson = new JSONObject();
					syncParamsJson.put("withSign", "0");
					
					// 个性化数据同步
					syncParamsJson.put("usr_id", "common");
					
					// business_id取值：contact（联系人）、location（地点）和word_list（词表）
					syncParamsJson.put("business_id", "word_list");
					
					// 待同步的数据，字符必须用utf-8编码
					byte[] syncData = "张三\r\n李四\r\n王五".getBytes("utf-8");
					
					uartAgent.sendMessage(PacketBuilder.
							obtainAIUICtrPacket(CMD_SYNC, SYNC_DATA_INDIVIDUAL, 
									0, syncParamsJson.toString(), syncData));
				}  catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if(intent.hasExtra("account")){
				JSONObject syncParamsJson = new JSONObject();
				try {
					syncParamsJson.put("withSign", "0");
					
					JSONObject syncDataJson = new JSONObject();
					syncDataJson.put("tuid", "123");
					syncDataJson.put("tplatform", "rsd");
					
					byte[] syncData = syncDataJson.toString().getBytes("utf-8");
					
					uartAgent.sendMessage(PacketBuilder.
							obtainAIUICtrPacket(CMD_SYNC, SYNC_DATA_ACCOUNT, 
									0, syncParamsJson.toString(), syncData));
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
			
		}
	}

}
