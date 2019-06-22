

This project is a prototype of a Kotlin multiple page application that 
shares code between server and client for rendering and state.

<img src="docs/app-screen.png" alt="Todo application" width="500"/>


## Sharing view rendering
The project uses Kotlinx.html for partial rendering on both client and 
server sides. Both sides use the code from a common module. On the server 
side, this code is integrated into the whole page rendering. On the client,
 this code generates a new DOM subtree to replace a portion of the page, 
 depending on user interaction.

## Sharing state
The application uses Kotlinx.serialization to share state between server 
and client. The page includes a JSON variable that represents the server 
state of the user's ToDos. The client-side deserializes it during page 
loading to build its state.

## Redux implementation
The project includes a Kotlin redux implementation with:
  - a generic store/action mechanism,
  - partial state subscribe/listening,
  - middleware.

An API middleware implements client-server communication.

The implementation simulates server error during removing of todo 
(one time in two).

## Build and launch
From the root directory, just launch:

```bash
 ./gradlew run
```

## Todo
 - use new MPP plugin,
