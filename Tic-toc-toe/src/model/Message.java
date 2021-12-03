package model;

import java.io.Serializable;

public enum Message implements Serializable{
	CREATE_SESSION,
    JOIN_SESSION,
    JOIN_SESSION_SUCCESS,
    UNVALID_ID,
	SESSION_IS_FULL,
	NICKNAME_EXISTS;
}
