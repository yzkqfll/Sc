/*
 * Copyright (C) 2015 Ganesh Mahendran <opensource.ganesh@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zgzhsh.smartcontroller;

public class ScConstants {
	public static final String BOARD_AP_SSID = "smarthome";

	public static final int BOARD_AP_UDP_SERVER_PORT = 9527;
	public static final int BOARD_STA_UDP_SERVER_PORT = 9528;

	/**
	 * Net Config
	 */
	public static final String NET_CONFIG_FILE = "net_config";

	/**
	 * Packet type
	 */
	public static final byte PKT_TYPE_SET_SSID = 1;
	public static final byte PKT_TYPE_QUERY_IP = 2;
	public static final byte PKT_TYPE_INFRA = 3;

	/**
	 * Packet subtype
	 */
	public static final byte PKT_SUBTYPE_NONE = 0;
	public static final byte PKT_SUBTYPE_IR_CONNECT = 1;
	public static final byte PKT_SUBTYPE_IR_ENABLE_ICC = 2;
	public static final byte PKT_SUBTYPE_IR_DECODE = 3;
	public static final byte PKT_SUBTYPE_IR_DISABLE_ICC = 4;
	public static final byte PKT_SUBTYPE_IR_SEND = 5;

	/**
	 * IR key name
	 */
	public static final String IR_KEY_POWER = "irkeypower";
	public static final String IR_KEY_CH_UP = "irkeychup";
	public static final String IR_KEY_CH_DOWN = "irkeychdown";
	public static final String IR_KEY_VOL_UP = "irkeyvolup";
	public static final String IR_KEY_VOL_DOWN = "irkeyvoldown";

}
