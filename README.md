# This project contains 2 sub projects

## 1. kimmer

   Port [https://github.com/immerjs/immer](https://github.com/immerjs/immer) for kotlin/jvm.
   
   [Click here](./doc/kimmer-core/README.md) to view more

## 2. kimmer-sql

   SQL DSL for kotlin

   - API perspective:
   
      As static as possible, find problems while compiling, bring kotlin null safety to SQL.
      
   - Functional perspective:
   
      As dynamic as possible, make complex problems easy to solve. Not only dynamic where predicates, but also dynamic table joins. Support Automatic SQL optimization too.
   
   [Click here](./doc/kimmer-sql/README.md) to view more
   
-----

# Other projects: 
[graphql-provider](https://github.com/babyfish-ct/graphql-provider): A GRM *(GraphQL Relation Mapping)* base on kimmer-sql, it allowes user to create graphql services base on RDBMS as fast as possible.
