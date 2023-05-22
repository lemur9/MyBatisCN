/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.mapping;

/**
 * @author Clinton Begin
 */
public enum StatementType {
  //STATEMENT：普通的Statement语句，即直接执行SQL语句。这是默认的执行方式。
  //PREPARED：PreparedStatement语句，使用占位符的方式来预编译SQL语句，可以提高性能并防止SQL注入攻击。
  //CALLABLE：可调用语句，用于执行存储过程或函数调用。
  STATEMENT, PREPARED, CALLABLE
}
