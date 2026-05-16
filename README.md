# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```
## Diagram link
https://sequencediagram.org/index.html#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5M9qBACu2AMQALADMABwATG4gMP7I9gAWYDoIPoYASij2SKoWckgQaJiIqKQAtAB85JQ0UABcMADaAAoA8mQAKgC6MAD0PgZQADpoAN4ARP2UaMAAtihjtWMwYwA0y7jqAO7QHAtLq8soM8BICHvLAL6YwjUwFazsXJT145NQ03PnB2MbqttQu0WyzWYyOJzOQLGVzYnG4sHuN1E9SgmWyYEoAAoMlkcpQMgBHVI5ACU12qojulVk8iUKnU9XsKDAAFUBhi3h8UKTqYplGpVJSjDpagAxJCcGCsyg8mA6SwwDmzMQ6FHAADWkoGME2SDA8QVA05MGACFVHHlKAAHmiNDzafy7gjySp6lKoDyySIVI7KjdnjAFKaUMBze11egAKKWlTYAgFT23Ur3YrmeqBJzBYbjObqYCMhbLCNQbx1A1TJXGoMh+XyNXoKFmTiYO189Q+qpelD1NA+BAIBMU+4tumqWogVXot3sgY87nae1t+7GWoKDgcTXS7QD71D+et0fj4PohQ+PUY4Cn+Kz5t7keC5er9cnvUexE7+4wp6l7FovFqXtYJ+cLtn6pavIaSpLPU+wgheertBAdZoFByyXAmlDtimGD1OEThOFmEwQZ8MDQcCyxwfECFISh+xXOgHCmF4vgBNA7CMjEIpwBG0hwAoMAADIQFkhRYcwTrUP6zRtF0vQGOo+RoFmipzGsvz-BwVygYKQH+iMyzKfMizfGpOx7Es0KPMBFRvl2MAIEJ4oYoJwkEkSYCkjZgrDvyDJMlOBlzjS+73sKMBihKboynKZbvEqN5BXeSa+s6XaRVunniVQSLwPEKAgBqaXyBiJkAsSMCQDAJUMRlVK3j5MCHnIKDPvE56Xte3mLpUy4BmuAbtelKU+jppbOeKGSqABmAjT6oEvPpRGGWM3wUVR9ZGTAmnVBhSXwMgqYwLh+GjIR5bEaRK2XmtyEkahDb0Yx3h+P4XgoOgMRxIkr3vc5vhYKJgpzY00gRvxEbtBG3Q9HJqgKcMq2Ieg6HwpUI31BR0BIAAXnkBT1AAPAjSHlNNlk7clnb1PZ9h-U5Ql-a5ajuduhi7gl9WMmALVtfBiNoIFvKJd1oXhU+A3yLK8pE0jnUCrtNn1C1r5DWzgv1RwKDcMel481e2gCwucvC-U0ia0yhgUcrnbtmjAn06eE1TTN8vbWBW0SfCyb7dhh14TAWZ0U2nhPQEKLrv42Dihq-FojAADiSoaADmWSXH4NQ-YSrw1dfPI9pZOlhjUDY7jaAE9LaAk87FNZS6MCc5ncwYnHTIJ3MjMkizXl1fS9dMtzFcG8FS4i+KYsvlussgSlivi8AXeq4bvlgG3ajxWrXVCvUovx0qVu16zH4F-ULcr4njsIIBBfTx781jI3agFg04wPwAktIBYAIzhMEgQgps8RdQoDdJyMyIJkigDVCAyCRkQQPwAHJKiMhcGAnR3aJi9iUMAOE8JZmWA-VQT8X5Knfl-H+f9lgAKAdAz4sDlgQPyjQpa3wEFIOWigtBjYGLB2Yv4DgAB2NwTgUBOBiBGYIcAuIADZ4ATkMKvGARRvZiRrpJVoHQM5Z2mDnJCWZWFzHQeTB4sJ-QVzWHpe+SpEG0OWptUmJiUYdgPvURq6JV4YjgHI1eHdmY1RkD3UcfcuY60HuvQ2IVt5j36hPeQYTh6ZWykrQa1tF77hcV4pUGJ9FcjiULLevV1yryivKbJC8j4OPqJ4o8KBvH-kvvYr8N8ah3zfh-eo39f52K0rtUSODjoWNaWQzpgduFMWepYTW9lNgfSQAkMAEy+wQGmQAKQgOKXecwYgMLVEorBgNXb1GaMyGSPQH7Z15ro0Y2AEDAAmVAOAEB7JQDWK0wxjjbZmJgBY65tzKAPKeXsAA6iwV+EMegACF+IKDgAAaRYSQtpMAOmBDsdXJx2UABWay0DuNWeKWphImYeRVrVdmvdOYDx0egIeeSeo7ySbEqeLtKbRL1rEvxstl7uJ+Xc-50AaUOhHpEiURTtCS0qjc3ljzoC5MFQkuuor2Ukv8WSwJirgBrEttoacZ0uSqUlX86VUBZWbx6hGDguoNkoBlDyw1TyYpGgqlqoqAUynlFtninFSoL5XwcU03SbzMLKL6QHLhj1eFeFuTMuZkb5SIGDLAYA2BrmEFLoo5OqjSwNBBmDCGUNjB53KV+E2oNwYRgaVZGu2UNZa2teCjEpta0+OJSk0lG9AkUr7DyDEArTWjwlIGBAKr55MvlbZE0CB96DjbUvIwZt0TdonR1AJESZDzotl25JB8bbHzXbW6Q4KfUVp2inN2hbKi9N9v0kZmAgA
