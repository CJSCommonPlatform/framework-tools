# Change Log
All notable changes to this project will be documented in this file, which follows the guidelines
on [Keep a CHANGELOG](http://keepachangelog.com/). This project adheres to
[Semantic Versioning](http://semver.org/).

## [Unreleased]

## [5.2.1] - 2019-05-09
### Fixed 
- Added event-subscription jar to list of jars to be excluded from Thorntail

## [5.2.0] - 2019-05-02
### Changed
- Upgraded Swarm to Thorntail
### Fixed 
- Add new event-store jars to list of jars to be excluded from Thorntail

## [5.1.1] - 2019-03-21
### Fixed
- Use correct maven scope for test jars
- Update to framework 5.1.2

## [5.1.0] - 2019-03-11
### Changed
- Update to framework 5.1.0

## [5.0.0] - 2018-11-19
### Changed
- Update to framework 5.0.4

## [4.5.0] - 2018-07-12
### Changed
- Update to framework version 4.3.2
- Update to framework api version 2.2.0
- Update schema catalog service to version 1.3.2

## [4.4.0] - 2018-06-22
### Changed
- Paging of the event stream now done using SQL rather than calling limit(...) on the stream
- Moved to Postgres for running the integration tests
- Test event listener now uses JDBC rather than Deltaspike/JPA

### Added
- added ```-Drun.it=false``` property to allow the integration tests to be skipped 

## [4.3.4] - 2018-06-12
### Changed
- Improved logging to include streamId and event metadata in MDC

## [4.3.3] - 2018-06-06
### Fixed
- Fix for replaying of events into custom event listeners
- Fixed logging for the replay application

## [4.3.2] - 2018-06-04
### Fixed
- Fix of running out of database Connections during the replay

## [4.3.1] - 2018-05-30

### Fixed
- Fix of event stream returning events in reverse order

## [4.3.0] - 2018-05-30

### Changed
- Events are loaded into memory in batches of 1000 at each time

## [4.2.1] - 2018-05-29

### Fixed
- Weld issues when starting Swarm

## [4.2.0] - 2018-05-25

### Fixed
- Fixed transaction timeout when replaying large numbers of events

## [4.1.0] -2018-05-01

### Changed
- Exclude schema catalog loader classes when merging WARs

## [4.0.0] - 2018-03-12

### Changed
- Update to framework version 4.0.0
- Update to framework api version 2.0.1
- Updated AsyncStreamDispatcher to support stream status source.

## [3.1.0] - 2018-01-26

### Added
- Replay tool manual

### Fixed
- Fix archive loader dependency exclusion to include additional libraries

### Changed
- Update to framework version 3.1.0
- Update to framework api version 1.1.0
- Update to common-bom version 1.22.0

## [3.0.0] - 2018-01-22

### Changed
- Update to use framework version 3.0.0

## [2.0.1] - 2017-11-09

### Fixed
- Corrected altered dependencies for framework 2.2.1

## [2.0.0] - 2017-09-15

### Changed
- Converted to Bintray released
- Upgraded to framework 2.2.1

### Added
- Coveralls added to build

## [1.2.0] - 2017-05-26

### Updated
- Skip missing handlers

## [1.1.0] - 2017-05-25

### Updated
- Bump framework version

## [1.0.0] - 2016-11-17

### Added
- Replay Event Stream tool

[Unreleased]: https://github.com/CJSCommonPlatform/framework-tools/compare/release-1.2.0...HEAD
[1.2.0]: https://github.com/CJSCommonPlatform/framework-tools/compare/release-1.1.0...release-1.2.0
[1.1.0]: https://github.com/CJSCommonPlatform/framework-tools/compare/release-1.0.0...release-1.1.0
[1.0.0]: https://github.com/CJSCommonPlatform/framework-tools/commits/release-1.0.0

