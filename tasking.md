## Bird View
- [x] no need for the container to construct: instance
- [ ] need for the container to construct: via constructor
- [ ] need for the container to construct: via field injection
- [ ] need for the container to construct: via method injection

## 02-22

- [x] `DependencyNotFoundException` should tell which `Component` lacks which `Dependency`
- [x] component construction | constructor injection | sad | transitive dependency not found
- [x] component construction | constructor injection | sad | self cyclic dependencies
- [x] component construction | constructor injection | sad | transitive cyclic dependencies

## 02-21

- [x] component construction | constructor injection | sad | cyclic dependencies

## 02-20

- [x] component construction | constructor injection | inject constructor (one-level dependency)
- [x] component construction | constructor injection | transitive dependency
- [x] component construction | constructor injection | sad | multi inject constructors
- [x] make `IllegalComponentException` an Enum
- [x] component construction | constructor injection | sad | no default nor inject constructors
- [x] component construction | constructor injection | sad | dependencies of constructor non-existent
- [x] component construction | constructor injection | sad | unregistered component

## 02-19

- [x] convert to JSR330 Provider

## 02-18

- [x] component construction | constructor injection | no args construction
- [x] convert to assert-j assertions
- [x] init local & remote repo

## Backlog

- component construction
- dependencies selection
- lifecycle management