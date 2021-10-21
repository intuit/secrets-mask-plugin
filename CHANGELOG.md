CHANGELOG
=========

1.12
-----

* Extend secrets masking feature to also work for Freestyle jobs
* Fix bug that caused hyperlinks to not work
* Reduce logger activity to only log the line when it actually detects a secret and masks it, as opposed to every line
* Logger preprends build metadata to the entry to make tracking in Splunk easier. Entry format is:
```
SecretsMaskUtil.secretsMask() [<PARENT_FOLDER> » <JOB_NAME> #<BUILD NUMBER>]: <LINE WITH MASKED SECRET>
```
* Underlying data structure storing `SecretPatterns` converted from `HashSet<SecretPattern>` to a `List<SecretPattern>` with a separate `HashMap<String, SecretPattern>` using the `SecretPattern.name` as the key. The HashMap is used for overriding patterns with the same name, while the List will be the source of truth for patterns to be used for matching. Can't directly use a HashMap for storing the secrets because it doesn't play well with Stapler and Jelly, which connects the configurations made from the Jenkins UI to the underlying code.

1.0
-----

* Initial implementation