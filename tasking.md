## Bird View

- [x] no need for the container to construct: instance
- [x] need for the container to construct: via constructor
- [x] need for the container to construct: via field injection
- [ ] need for the container to construct: via method injection

## 03-05

- [x] method injection | could invoke super classes' injected methods in order
- [x] method injection | sub overrides super | both annotated -> sub overrides super
- [x] method injection | sub overrides super | super annotated while sub was not -> shall not inject for sub
- [x] method injection | sub overrides super | super not annotated while sub was -> shall inject for sub only
- [x] constructor inj | sad path | abstract class
- [x] constructor inj | sad path | interface
- [ ] field injection | sad path | `final`
- [ ] method injection | sad path | generics

## 03-04

- [x] method injection | method will be called anyway
- [x] method injection | dependency could be injected via method injection

## 03-01

- [x] field injection | should_inject_dependency_via_field
- [x] field injection | should_throw_exception_when_field_has_cyclic_dependencies

## 02-28

- [x] refactor: merge `dependencies` back to `providers`

## 02-24

- [x] refactor: split `Context` to `Context` and `ContextConfig`
- [ ] validate inside `contextConfig.getContext()`
    - IllegalComponent (e.g. multi @Inject constructors)
    - [x] NotFoundDependency
    - [x] Cyclic (sol. graph algo - does cyclic loop exist)
- [x] fix: `map(typeKey -> getContext()....))` creates a new Context object every time
    - solution: use bespoke Provider interface
- [x] refactor: replace Jakarta's inject Provider to the customized one

## 02-22

- [x] ~~tell which dep causes cyclic dep~~ tell which components are involved in a CyclicDependenciesFoundException
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

- try using filed injection for `ConstructionInjectionProvider`, i.e. `Context` injected
  to `ConstructionInjectionProvider`
- component construction
- dependencies selection
- lifecycle management