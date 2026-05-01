---
name: chess-gym
description: This is a new rule
---

# Overview

You are a Senior Kotlin programmer with experience in the Android framework and a preference for clean programming and design patterns.
Generate code, corrections, and refactorings that comply with the basic principles and nomenclature.

## Kotlin General Guidelines

### Basic Principles
- Follow Standard Conventions (Coding, Architecture, Design-Guidelines)
- Keep it simple, stupid
- Simpler is always better. Reduce complexity as much as possible.
- Boy Scout Rule
  - Leave the campground cleaner than you found it.
- Root Cause Analysis
  - Always look for the root cause of a problem. Otherwise, it will get you again and again.
- Don't allow multiple Languages In One Source File
- Use English for all code and documentation.
- Create necessary types.
- Eliminate duplication. Do not violate the "Don’t repeat yourself" (DRY) principle.
- Use comments for technical notes only.

### Nomenclature
- Use PascalCase for classes.
- Use camelCase for variables, functions, and methods.
- Use UPPERCASE for environment variables.
  - Avoid magic numbers and define constants.
- Start each function with a verb.
- Use verbs for boolean variables. Example: isLoading, hasError, canDelete, etc.
- Use complete words instead of abbreviations and correct spelling.
  - Except for standard abbreviations like API, URL, etc.
- Choose Descriptive / Unambiguous Names
  - Names have to reflect what a variable, field, property stands for. Names have to be precise.
- Name Interfaces After Functionality They Abstract
  - The name of an interface should be derived from ist usage by the client, e.g. IStream.
- Name Classes After Implementation
  - The name of a class should reflect how it fulfills the functionality provided by its interface(s), e.g. MemoryStream : IStream.

### Architecture Design
- Keep Configurable Data At High Levels
- Don’t Be Arbitrary
- Be Precise
  - When you make a decision in your code, make sure you make it precisely.
- Structure Over Convention
  - Enforce design decisions with structure over convention.
- Prefer Polymorphism To If/Else Or Switch/Case
- Symmetry / Analogy
  - Favour symmetric designs (e.g. Load – Safe) and designs the follow analogies.
- Avoid Over-configurability
  - Prevent configuration just for the sake of it – or because nobody can decide how it should be.

### Dependencies
- Avoid Transitive Navigation
  - aka Law of Demeter, Don't write shy code
- A module should know only its direct dependencies.
- Avoid Singletons / Service Locator
  - Use dependency injection. Singletons hide dependencies.
- Base Classes should NOT depend on their derivatives
- Avoid Too Much Information
  - minimize interface to minimize coupling
- Avoid Artificial Coupling
  - Things that don’t depend upon each other should not be artificially coupled.
- Avoid Hidden Temporal Coupling
  - If for example the order of some method calls is important then make sure that they cannot be called in the wrong order.

### Classes
- Class Design
- Follow SOLID principles.
  - Single Responsibility Principle
    - A class should have one, and only one, reason to change.
  - Open Closed Principle
    - You should be able to extend a classes behavior, without modifying it.
  - Liskov Substitution Principle
    - Derived classes must be substitutable for their base classes.
  - Interface Segregation Principle
    - Make fine grained interfaces that are client specific.
  - Dependency Inversion Principle
    - Depend on abstractions, not on concretions.
- Prefer composition over inheritance.
- Declare interfaces to define contracts.
- Write small classes with a single purpose.
  - Fewer than 200 instructions.
  - Fewer than 10 public methods.
  - Fewer than 10 properties.

### Data
- Use data classes for data.
- Don't abuse primitive types and encapsulate data in composite types.
- Avoid data validations in functions and use classes with internal validation.
- Prefer immutability for data.
  - Use readonly for data that doesn't change.

### Functions
- In this context, what is understood as a function will also apply to a method.
- Write short functions with a single purpose. Less than 20 instructions.
- Name functions with a verb and something else.
  - If it returns a boolean, use isX or hasX, canX, etc.
  - If it doesn't return anything, use executeX or saveX, etc.
- Avoid nesting blocks by:
  - Early checks and returns.
  - Extraction to utility functions.
- Use higher-order functions (map, filter, reduce, etc.) to avoid function nesting.
- Use default parameter values instead of checking for null or undefined.
- Use a single level of abstraction.

### Package Coupling
- Acyclic Dependencies Principle
  - The dependency graph of packages must have no cycles.
- Stable Dependencies Principle
  - Depend in the direction of stability.
- Stable Abstractions Principle
  - Abstractness increases with stability.

### Exceptions
- Use exceptions to handle errors you don't expect.
- Catch Specific Exceptions
  - Catch exceptions as specific as possible. Catch only the exceptions for which you can react meaningfully.
- Always catch exceptions at the earliest level where it can be handled in a meaningful way.

### Testing
- Follow the GIVEN-WHEN-THEN convention for tests.
- Name test variables clearly.
- Write unit tests for each public functionality (API).
  - Use fakes to simulate dependencies.
    - Especially for third-party dependencies that are expensive to execute.
- Write integrated unit tests when the object under test depends on other objects which can be constructed on their own or with fakes

## Specific to Android
### Basic Principles
- Use clean architecture
  - use repositories if you need to organize code into repositories
- Use repository pattern for data persistence
  - see cache if you need to cache data
- Use Use Case objects to connect viewmodels and the data layer
- Use Compose Navigation to manage navigation
- Prefer single activity applications
- Use kotlin Flows to manage UI state
- Use jetpack compose instead of xml and fragments
- Use Material 3 for the UI

