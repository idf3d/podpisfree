/*
 *   Copyright (C) 2023 <https://github.com/idf3d>
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package pl.podpisfree;

import pl.podpisfree.api.Server;

public class Main {
  public static void main(String[] args) {
    Server server = new Server();

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      server.stop();
      System.out.println("\n\nBye.");
    }));

    server.run();
  }
}
