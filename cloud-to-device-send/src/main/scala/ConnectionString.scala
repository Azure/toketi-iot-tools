// Copyright (c) Microsoft. All rights reserved.

object ConnectionString {

  /** Format a C2D connection string accordingly to SDK
    *
    * @param accessHostName Service host name
    * @param accessPolicy   Authorization policy name
    * @param accessKey      Authorization key
    *
    * @return A connection string
    */
  def build(accessHostName: String, accessPolicy: String, accessKey: String): String = {
    s"HostName=${accessHostName};SharedAccessKeyName=${accessPolicy};SharedAccessKey=${accessKey}"
  }
}
