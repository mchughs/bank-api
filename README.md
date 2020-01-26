# Bank-API / Samuel McHugh

## Working features

- [X] Create a bank account
- [x] View a bank account
- [x] Deposit into a bank account
- [x] Withdraw money from a bank account
- [X] Transfer money between existing accounts
- [X] Retrieve account audit logs
- [X] Thread-safe account creation

## Missing features
- [ ] Thread-safe account mutation
- [ ] Persistence

## Run the server

To start a web server for the application, run:

    $ lein ring server-headless

You can change the port being used by pre-pending the command with a PORT environmental variable such as

    $ PORT=1234 lein ring server-headless

You can also run the function `bank-api.core/start-server` in your repl if you prefer.

To confirm the server is up and running you should be able to run the command

    $ curl localhost:3000/status

and get back the answer "OK".

## Tests

To run tests simply call

    $ lein test

This will run the unit tests as well as the concurrency tests. The concurrency tests
are using a library called clj-gatling which will print some results to the terminal.
