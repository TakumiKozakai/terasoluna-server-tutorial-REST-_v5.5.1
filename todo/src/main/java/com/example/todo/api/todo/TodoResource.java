package com.example.todo.api.todo;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class TodoResource implements Serializable {

	private static final long serialVersionUID = 1L;

	private String todoId;

	@NotNull
	@Size(min = 1, max = 30)
	private String todoTitle;

	private boolean finished;

	private Date createdAt;

}
