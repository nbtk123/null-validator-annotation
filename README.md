# null-validator-annotation
Annotate your class, and it will generate null-validation code for that class. Useful for doing a validation layer over your input.

Assume you have the following data class that represents a User:

```
data class User {
  val id: String,
  val username: String,
  val email: String,
  val optionalData: String?
}
```

And you have a JSON string input that represents a User, but it's partial:
```
{
  "id": "123456",
  "username": "yolo123"
}
```

When this JSON is converted to the data class, the **optionalData** field is ```NULL``` which is OK, but the **email** field is also ```NULL``` even though it's non-nullable.

Eventually you'll end up writing a validation layer:

```
data class User {
  ...
  fun validate() {
    id != null && username != null && email != null
  }
}
```

This is where the **null-validator-annotation** comes handy - it generates the ```validate()``` function as an extension function for your class.

For the following code:

```
@NullValidatorClass
data class User {
  val id: String,
  val username: String,
  val email: String,
  val optionalData: String?
}
```

The following code will be generated:
```
fun User.validate(): Boolean {
  val result = id != null && username != null && email != null && optionalData != null
  return result
}
```

And if you want to validate **only some of the fields**:
```
@NullValidatorClass
data class User {
  @NullValidatorField val id: String,
  @NullValidatorField val username: String,
  @NullValidatorField val email: String,
  val optionalData: String?
}
```

Then the following code will be generated:
```
fun User.validate(): Boolean {
  val result = id != null && username != null && email != null
  return result
}
```
