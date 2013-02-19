/*******************************************************************************
 * Copyright (c) 2010-2013 Foos-Bar.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Kevin Kelley - initial API and implementation
 *******************************************************************************/
package com.foosbar.mailsnag.smtp;

/**
 * The status of the SMTP Server
 * 
 * @author Kevin Kelley
 * 
 */
public enum ServerState {
	STARTING, LISTENING, STOPPED;

	public boolean isListening() {
		return this == LISTENING;
	}
}
