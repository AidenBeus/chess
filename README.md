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
https://sequencediagram.org/index.html#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5M9qBACu2AMQALADMABwATG4gMP7I9gAWYDoIPoYASij2SKoWckgQaJiIqKQAtAB85JQ0UABcMADaAAoA8mQAKgC6MAD0PgZQADpoAN4ARP2UaMAAtihjtWMwYwA0y7jqAO7QHAtLq8soM8BICHvLAL6YwjUwFazsXJT145NQ03PnB2MbqttQu0WyzWYyOJzOQLGVzYnG4sHuN1E9SgmWyYEoAAoMlkcpQMgBHVI5ACU12qojulVk8iUKnU9XsKDAAFUBhi3h8UKTqYplGpVJSjDpagAxJCcGCsyg8mA6SwwDmzMQ6FHAADWkoGME2SDA8QVA05MGACFVHHlKAAHmiNDzafy7gjySp6lKoDyySIVI7KjdnjAFKaUMBze11egAKKWlTYAgFT23Ur3YrmeqBJzBYbjObqYCMhbLCNQbx1A1TJXGoMh+XyNXoKFmTiYO189Q+qpelD1NA+BAIBMU+4tumqWogVXot3sgY87nae1t+7GWoKDgcTXS7QD71D+et0fj4PohQ+PUY4Cn+Kz5t7keC5er9cnvUexE7+4wp6l7FovFqXtYJ+cLtn6pavIaSpLPU+wgheertBAdZoFByyXAmlDtimGD1OEThOFmEwQZ8MDQcCyxwfECFISh+xXOgHCmF4vgBNA7CMjEIpwBG0hwAoMAADIQFkhRYcwTrUP6zRtF0vQGOo+RoFmipzGsvz-BwVygYKQH+iMyzKfMizfGpOx7Es0KPMBFRvl2MAIEJ4oYoJwkEkSYCkjZgrDvyDJMlOBlzjS+73sKMBihKboynKZbvEqN5BXeSa+s6XaRVunm7glPkwIecgoM+8Tnpe17eYulTLgGa4BsV6UpT6Omls54oZKoAGYA1PqgS8+lEYZYzfBRVH1kZMCadUGFJfAyCpjAuH4aMhHlsRpEDZeQ3ISRqENvRjHeH4-heCg6AxHEiSHcdzm+FgomCl1jTSBG-ERu0EbdD0cmqApwyDYh6DofClQNfUFHQEgABeeQFPUAA8P1IeU7WWRNyWdvU9n2FdTlCVdrlqO526GJlvIjr5YAFUV8G-WggXEw6S6heFT41fIsrynDf2lQKk02fUBWvnVRMLqORgoNwx6XhTV7aDTQshfU0ii0yhgUfznbtkDAnY6eLVtR13PjWBY0SfCybTdhs14TAWZ0U2nh7QEKLrv42Dihq-FojAADiSoaDd4k1PUDSe89b32Eq31rVT-3aUjpYg1A4OQ2gMPs2gCN6yjVBIjAjJgGHcwYp7TLe3MuMkgTXm3tlufk6nMvBfT9SM9VL5bpzIEpbzzPABXgv7qTJdqJLPLTktXL14l5UM+K66D1FbPd6PsVzB5AsfrH9RF2Ag+qDrCCAbHHfG91Yz52oBYNOMZ8AJLSAWACM4TBIEIKbPEuooG6nJmSCySgGqX9IJGRBGfAAckqIyFwYCdCNomU2JQwA4TwlmZYZ9VAXyvkqW+D8n4v2WG-D+gDPjAOWH-EAADeo-1QUqcBcxIHQJtgxO2zF-AcAAOxuCcCgJwMQIzBDgFxAAbPACchhB4wCKGbMSmdJKtA6KHcO0xI5ISzGApUsDkYPFhP6VOaw9KnxoUA-qo1EbaIBh2LOLocqiMHhiOANilRl3xhlKkVd6Q5yZLXZR6AJ50ynk3GeLcpbyHirTRc-ts581qmrPuJNrFHhQLYlW2gl6cmJGsNRK9Qmy0bpVWeSp57GkXgFDJhi5i93XmY+o9iEmDz3gfMxR8A4wH0TfO+9RH7PxMVpSaokkHzVaVg9pMBOmBBMTtZh+1LCi3spsE6SAEhgGmX2CAcyABSEBxRewrP4MhapJEINugbQOTRmQyR6GfCOlMVGjGwAgYA0yoBwAgPZKApS5i3w0eYjWuiWnrHuY855ry9gAHUWDXxej0AAQvxBQcAADS3w2k4K6RZRpESrEACtNloFsRs8UdSUCEjxqvGJrisruJrhLOu2SG7+LCoEqJIT2761RkE1WljCbkrCcLXOSTim9XSTAO5DzKBAugL4sqQoAkSjntoVmRTW7yFSUqIVIrAUvOgBU8oGt8W4scf+fepivxNN0l8zCUj+nW0bEwpi+0vAPPmYsh18pEDBlgMAbAdzCBJwkX7GRpYGgPSei9N6xho6VK-PLR6z0IzGqspnbOHBFbomkFCjECsxYoCcaSzllcKW8s8X2Eekqub0uboGBAMg25uK5hi2yJoEAcsHNyoW9Rk1ZpHo2kqta5YyBTcrYt0S82TQ1pmpWab6nxomv7M1EbKh9ItgMxhmAgA
