/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jp.l1j.telnet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import jp.l1j.server.utils.StreamUtil;
import jp.l1j.telnet.command.TelnetCommandExecutor;
import jp.l1j.telnet.command.TelnetCommandResult;

public class TelnetConnection {
	private class ConnectionThread extends Thread {
		private Socket _socket;

		public ConnectionThread(Socket sock) {
			_socket = sock;
		}

		@Override
		public void run() {
			InputStreamReader isr = null;
			BufferedReader in = null;
			OutputStreamWriter osw = null;
			BufferedWriter out = null;
			try {
				isr = new InputStreamReader(_socket.getInputStream());
				in = new BufferedReader(isr);
				osw = new OutputStreamWriter(_socket.getOutputStream());
				out = new BufferedWriter(osw);

				String cmd = null;
				while (null != (cmd = in.readLine())) {
					TelnetCommandResult result = TelnetCommandExecutor
							.getInstance().execute(cmd);
					out.write(result.getCode() + " " + result.getCodeMessage()
							+ "\r\n");
					out.write(result.getResult() + "\r\n");
					out.flush();
					// // for debug
					// System.out.println(result.getCode() + " " +
					// result.getCodeMessage());
					// System.out.println(result.getResult());
				}
			} catch (IOException e) {
				StreamUtil.close(isr, in);
				StreamUtil.close(osw, out);
			}
			try {
				_socket.close();
			} catch (IOException e) {
			}
		}
	}

	public TelnetConnection(Socket sock) {
		new ConnectionThread(sock).start();
	}
}
