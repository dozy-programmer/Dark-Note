# Prints some helpful hints
-verbose

-keepattributes SourceFile,LineNumberTable,Exceptions,InnerClasses,Signature,Deprecated,*Annotation*,EnclosingMethod

# Firebase
-keepclassmembers class com.akapps.dailynote.classes.data.firebase** {*;}

# Realm db
-keep class io.realm.annotations.RealmModule { *; }
-keep interface io.realm.annotations.RealmModule { *; }
-keep @io.realm.annotations.RealmModule class *
-keep class io.realm.internal.Keep
-keep @io.realm.internal.Keep class *
-keepnames public class * extends io.realm.RealmObject
-keep class * extends io.realm.RealmObject
-dontwarn javax.**
-dontwarn io.realm.**