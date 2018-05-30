# Change Log
All notable changes to this project will be documented in this file, which follows the guidelines
on [Keep a CHANGELOG](http://keepachangelog.com/). This project adheres to
[Semantic Versioning](http://semver.org/).

## [Unreleased]

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

