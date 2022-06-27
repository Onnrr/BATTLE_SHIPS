# BATTLE_SHIPS

## Input formats
* Create Account -> "create [userName] [password] [mail]"
* Login Check -> "login [userName] [password]"
* Invite other player -> "invite [otherPlayersID]"
* Decline game invite -> "decline_game [userID] [invitorID]"
* Disconnect -> "disconnect"

## Output formats
* Failed login or signup -> "FAIL"
* Successful login or signup -> "SUCCES"
* User information -> "INFO [userID] [userName] [userScore] [mail]"
* New player connected -> "CONNECTED [userID] [userName]"
* Player disconnected -> "DISCONNECTED [userName]"
* List of online players -> "ONLINE_PLAYERS [id] [userName] [status] ..... [id] [userName] [status]"
* Invitation -> "INVITATION [userID] [userName]"
* Declined game invite -> "DECLINED [userID]"
