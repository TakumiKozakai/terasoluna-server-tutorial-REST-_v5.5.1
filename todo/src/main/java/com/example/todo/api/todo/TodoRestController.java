package com.example.todo.api.todo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.todo.domain.model.Todo;
import com.example.todo.domain.service.todo.TodoService;
import com.github.dozermapper.core.Mapper;

@RestController
/**
 * リソースのパスを指定する。
 * /api/v1/の部分はweb.xmlに定義しているため、この設定を行うことで/<contextPath>/api/v1/todosというパスにマッピングされる。
 */
@RequestMapping("todos")
public class TodoRestController {

	@Inject
	TodoService todoService;
	@Inject
	Mapper beanMapper;

	@GetMapping
	// 応答するHTTPステータスコードを@ResponseStatusアノテーションに指定する。
	// HTTPステータスとして、”200 OK”を設定するため、value属性にはHttpStatus.OKを設定する。
	@ResponseStatus(HttpStatus.OK)
	public List<TodoResource> getTodos() {

		Collection<Todo> todos = todoService.findAll();
		List<TodoResource> todoResources = new ArrayList<>();
		for (Todo todo : todos) {
			// TodoServiceのfindAllメソッドから返却されたTodoオブジェクトを、応答するJSONを表現するTodoResource型のオブジェクトに変換する。
			// TodoとTodoResourceの変換処理は、Dozerのcom.github.dozermapper.core.Mapperインタフェースを使うと便利である。
			todoResources.add(beanMapper.map(todo, TodoResource.class));
		}
		// List<TodoResource>オブジェクトを返却することで、
		// spring-mvc-rest.xmlに定義したMappingJackson2HttpMessageConverterによってJSONにシリアライズされる。
		return todoResources;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TodoResource postTodos(@RequestBody @Validated TodoResource todoResource) {

		Todo createdTodo = todoService.create(beanMapper.map(todoResource, Todo.class));
		TodoResource createdTodoResponse = beanMapper.map(createdTodo, TodoResource.class);
		return createdTodoResponse;
	}

	// メソッドがGETのリクエストを処理するために、@GetMappingアノテーションを設定する。
	// パスからtodoIdを取得するために、value属性にパス変数を指定する。
	@GetMapping("{todoId}")
	@ResponseStatus(HttpStatus.OK)
	// @PathVariableアノテーションのvalue属性に、todoIdを取得するためのパス変数名を指定する。
	public TodoResource getTodo(@PathVariable("todoId") String todoId) {

		Todo todo = todoService.findOne(todoId);
		TodoResource todoResource = beanMapper.map(todo, TodoResource.class);
		return todoResource;
	}

	@PutMapping("{todoId}")
	@ResponseStatus(HttpStatus.OK)
	public TodoResource putTodo(@PathVariable("todoId") String todoId) {

		Todo finishedTodo = todoService.finish(todoId);
		TodoResource finishedTodoResource = beanMapper.map(finishedTodo, TodoResource.class);
		return finishedTodoResource;
	}

	@DeleteMapping("{todoId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteTodo(@PathVariable("todoId") String todoId) {

		todoService.delete(todoId);
	}

}
