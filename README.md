# BATTLE_SHIPS

## Input formats
* Create Account -> "create [userName] [password] [mail]"
* Login Check -> "login [userName] [password]"
* Invite other player -> "invite [otherPlayersID]"
* Decline game invite -> "decline_game [userID] [invitorID]"
* Accept game invite -> "accept_game [userID] [invitorID]"
* Send message -> "message [yourMessage]"
* Disconnect -> "disconnect"
* Delete Account -> "delete"
* Leave game -> "leave"
* Ready for game -> "ready"

* Location guess -> "guess [row][column]"

## Output formats
* Failed login or signup -> "FAIL"
* Successful login or signup -> "SUCCES"
* User information -> "INFO [userID] [userName] [userScore] [mail]"
* New player connected -> "CONNECTED [userID] [userName]"
* Player disconnected -> "DISCONNECTED [userID] [userName]"
* List of online players -> "ONLINE_PLAYERS [id] [userName] [status] ..... [id] [userName] [status]"
* Top 5 players -> "RANK [userName] [score] ... [userName] [score]"
* Invitation -> "INVITATION [userID] [userName]"
* Cannot start game -> "GAME_FAIL [otherID]"
* Game is starting -> "GAME_START [opponentID]"
* Declined game invite -> "DECLINED [userID]"
* Accepted game invite -> "ACCEPTED [userID]"
* New Message -> "MESSAGE [senderID] [senderName] [message]"
* Permission to leave -> "LEAVE"

* Opponent disconnected -> "OPPONENT_DISCONNECTED"
* Opponent ready -> "OPPONENT_READY"
* Location guess -> "GUESS [row][column]"

TODO send all players player joined game