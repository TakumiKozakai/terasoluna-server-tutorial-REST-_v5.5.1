package com.example.todo.domain.service.todo;

import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.terasoluna.gfw.common.exception.BusinessException;
import org.terasoluna.gfw.common.exception.ResourceNotFoundException;
import org.terasoluna.gfw.common.message.ResultMessage;
import org.terasoluna.gfw.common.message.ResultMessages;

import com.example.todo.domain.model.Todo;
import com.example.todo.domain.repository.todo.TodoRepository;

@Service
/*
 クラスレベルに、@Transactionalアノテーションをつけることで、公開メソッドをすべてトランザクション管理する。
 アノテーションを付与することで、メソッド開始時にトランザクションを開始、メソッド正常終了時にトランザクションのコミットが行われる。
 また、途中で非検査例外が発生した場合は、トランザクションがロールバックされる。
 */
@Transactional
public class TodoServiceImpl implements TodoService {

	private static final long MAX_UNFINISHED_COUNT = 5;

	@Inject
	TodoRepository todoRepository;

	@Override
	@Transactional(readOnly = true)
	public Todo findOne(String todoId) {
		Todo todo = todoRepository.findById(todoId).orElse(null);
		if (todo == null) {
			ResultMessages messages = ResultMessages.error();
			messages.add(
					ResultMessage.fromText(
							"[E404] The requested Todo is not found. (id=" + todoId + ")"));
			// 対象データが存在しない場合は、ResourceNotFoundException
			throw new ResourceNotFoundException(messages);
		}
		return todo;
	}

	@Override
	// MyBatisでは、↓の設定(readOnly = true)によりトランザクション制御の最適化が行われる
	@Transactional(readOnly = true)
	public Collection<Todo> findAll() {
		return todoRepository.findAll();
	}

	@Override
	public Todo create(Todo todo) {
		long unfinishedCount = todoRepository.countByFinished(false);
		if (unfinishedCount >= MAX_UNFINISHED_COUNT) {
			ResultMessages messages = ResultMessages.error();
			messages.add(
					ResultMessage.fromText(
							"[E001] The count of un-finished Todo must not be over " + MAX_UNFINISHED_COUNT + "."));
			// 業務エラーの場合は、BusinessException
			throw new BusinessException(messages);
		}
		String todoId = UUID.randomUUID().toString();
		Date createdAt = new Date();

		todo.setTodoId(todoId);
		todo.setCreatedAt(createdAt);
		todo.setFinished(false);

		todoRepository.create(todo);

		return todo;
	}

	@Override
	public Todo finish(String todoId) {
		Todo todo = findOne(todoId);
		if (todo.isFinished()) {
			ResultMessages messages = ResultMessages.error();
			messages.add(
					ResultMessage.fromText(
							"[E002] The requested Todo is already finished. (id=" + todoId + ")"));
			throw new BusinessException(messages);
		}
		todo.setFinished(true);
		todoRepository.updateById(todo);

		return todo;
	}

	@Override
	public void delete(String todoId) {
		Todo todo = findOne(todoId);
		todoRepository.deleteById(todo);
	}

}
