#  Notes

1. Switch from notification messages to data messages because they are handled in the same service
2. Check if the user is signed in..
   a) if the user is signed in, upon click open the link, show link in preview
   b) if the user is NOT signed in, open login, and upon successful login with the same userid as the notification was sent out to, open
      (else discard because wrong user post login)

3. Upon login check if the signing in user was the last signed in user. if not remove token from linked device.
  a) if another user signes in he has the same id but removes it from the old users device
     check if the user was signed in before and whether his last used device token still matches one
     of his current devices. auto choose.
     else show chooser and create
  b) if the same user signes in again, have a copy of the last used token per userid stored and
     check whether the last used device token for this user matches a stored device token.
     a) if it does, auto choose this device and update token on the server, update last used token
     b) if it does not, show chooser with create action


explanation:
  if the user is signed in, show link and open link upon click

  if the user signs out and sends a link, hide link content and request login. if userid = last logged in userid
  open link and reconnect device with still existing device id.

  if the user signs out and sends a link, but another was logged in in the meantime, the message is not deliverd
  because the tolken should have been deleted with the login of the second user

  if the user signs out and sings back in, remember the device id because the last used userid matches
  the now logging in userid. that way we can keep the device token, and auto reconnect to the last device

  if the user signs out and another user signs in, check that the last used device id no longer matches the new users
  and call delete token to not receive the old users pushes (because the token is still active).