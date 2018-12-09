# lib-databsae-runtime-core-compileonly

The database architecture is designed to seperate the database itself from the underlying 
implementation. This module provides compile time stubs, so that core code can be compiled. The
underlying implementation (e.g. lib-database-runtime-jdbc, or lib-database-runtime-android)
then provides the actual implementation of builder objects etc.

