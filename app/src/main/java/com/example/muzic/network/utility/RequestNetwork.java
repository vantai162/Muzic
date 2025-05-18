package com.example.muzic.network.utility;

import android.app.Activity;
import android.util.Log;

import java.util.HashMap;

public class RequestNetwork {
	private HashMap<String, Object> params = new HashMap<>();
	private HashMap<String, Object> headers = new HashMap<>();
	
	private final Activity activity;
	
	private int requestType = 0;
	
	public RequestNetwork(Activity activity) {
		this.activity = activity;
	}
	
	public void setHeaders(HashMap<String, Object> headers) {
		this.headers = headers;
	}
	
	public void setParams(HashMap<String, Object> params, int requestType) {
		this.params = params;
		this.requestType = requestType;
	}
	
	public HashMap<String, Object> getParams() {
		return params;
	}
	
	public HashMap<String, Object> getHeaders() {
		return headers;
	}
	
	public Activity getActivity() {
		return activity;
	}
	
	public int getRequestType() {
		return requestType;
	}
	
	public void startRequestNetwork(String method, String url, String tag, RequestListener requestListener) {
		Log.i("RequestNetwork.java", "startRequestNetwork: " +
				"\tmethod: " + method +
				"\turl: " + url +
				"\tHeaders: " + getHeaders() +
				"\tParams: " + getParams()
		);
		RequestNetworkController.getInstance().execute(this, method, url, tag, requestListener);
	}
	
	public interface RequestListener {
		public void onResponse(String tag, String response, HashMap<String, Object> responseHeaders);
		public void onErrorResponse(String tag, String message);
	}
}

//RequestNetwork.java là một lớp trung gian giúp chuẩn hóa việc gửi yêu cầu HTTP trong Android, thuận tiện cho việc tái sử dụng, kiểm soát headers, params và dễ dàng gắn listener để xử lý kết quả phản hồi.
